package jp.seraphyware.textencodechanger.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * テキスト変換サービス.
 *
 * @author seraphy
 */
@Component
public class TextEncodeConvService {

    /**
     * ロガー.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 文字コード一覧
     */
    private List<EncodingType> encodingTypes;
    
    /**
     * 文字コード一覧(検査順)
     */
    private List<EncodingType> checkEncodingOrder;

    /**
     * 文字コード一覧を返す.
     *
     * @return 対象とする文字コードのリスト
     */
    public final List<EncodingType> getEncodings() {
        if (encodingTypes == null) {
            encodingTypes = Collections.unmodifiableList(
                    Arrays.asList(EncodingType.values()));
        }
        return encodingTypes;
    }
    
    public List<EncodingType> getCheckEncodingOrder() {
        if (checkEncodingOrder == null) {
            checkEncodingOrder = getEncodings().stream()
                    .sorted((a, b) -> {
                // BOMつきの場合を優先してチェックする.
                // (JavaAPIはBOM有無にかかわらず読み込むため、先にBOM有無を判定させる)
                int bom_a = (a.getBOMLength() != 0) ? 1 : 0;
                int bom_b = (b.getBOMLength() != 0) ? 1 : 0;
                int ret = bom_b - bom_a;
                if (ret == 0) {
                    // BOM有、BOM無同士であれば、定義順で判定する。
                    ret = a.ordinal() - b.ordinal();
                }
                return ret;
            }).collect(Collectors.toList());
        }
        return checkEncodingOrder;
    }

    /**
     * 指定したファイルの文字コードを推定する. 不明な場合はnullを返す.
     *
     * @param file ファイル
     * @return 文字コード
     * @throws IOException 失敗
     */
    public final EncodingType presumeEncoding(final Path file) throws IOException {
        Objects.requireNonNull(file);

        log.debug("load: " + file);
        byte[] data = Files.readAllBytes(file);
        ByteBuffer byteBuf = ByteBuffer.wrap(data);

        return presumeEncoding(byteBuf);
    }

    /**
     * 指定したバイトバッファ内の文字コードを推定する. 不明な場合はnullを返す.
     *
     * @param byteBuf バイトバッファ
     * @return 文字コード
     * @throws IOException 失敗
     */
    public final EncodingType presumeEncoding(
            final ByteBuffer byteBuf
    ) throws IOException {
        Objects.requireNonNull(byteBuf);

        EncodingType encodingOk = null;
        for (EncodingType enc : getCheckEncodingOrder()) {
            Charset cs = enc.getCharset();
            CharsetDecoder dec = cs.newDecoder();
            dec.onUnmappableCharacter(CodingErrorAction.REPORT);
            dec.onMalformedInput(CodingErrorAction.REPORT);

            // 再読み込みのために位置を巻き戻す
            byteBuf.rewind();
            
            // エンコード可能か検証する
            if (enc.checkEncodable(byteBuf)) {
                encodingOk = enc;
                break;

            } else {
                log.info("coding error: " + enc);
            }
        }
        log.debug("-" + encodingOk);
        return encodingOk;
    }

    /**
     * テキストの変換を行う.
     */
    @FunctionalInterface
    public interface FileEncodingConverter {

        /**
         * ファイルの相対パスと現在の文字コードを指定して変換する.
         *
         * @param relativePathStr 相対パス
         * @param srcEncoding ファイルの文字コード
         * @throws IOException 失敗
         */
        void convert(
                String relativePathStr,
                EncodingType srcEncoding
        ) throws IOException;
    }

    /**
     * テキストの変換を行うコンバータを作成して返すファクトリ.
     *
     * @param srcDir 入力元ディレクトリ
     * @param destDir 出力先ディレクトリ
     * @param transferType 転送モード
     * @param reqBak バックアップの要否
     * @param destEncoding 出力文字コード
     * @return コンバータ
     */
    public final FileEncodingConverter createFileEncodingConverter(
            final String srcDir,
            final String destDir,
            final TransferType transferType,
            final boolean reqBak,
            final EncodingType destEncoding
    ) {
        Objects.requireNonNull(srcDir);
        Objects.requireNonNull(destEncoding);
        Objects.requireNonNull(transferType);

        Path srcBaseDir = Paths.get(srcDir);
        Path destBaseDir;
        if (destDir != null && transferType != TransferType.REPLACE) {
            destBaseDir = Paths.get(destDir);
        } else {
            destBaseDir = srcBaseDir;
        }

        return (relativePathStr, srcEncoding) -> {
            Objects.requireNonNull(relativePathStr);
            Objects.requireNonNull(srcEncoding);
            log.info("convert from " + relativePathStr + " : " + srcEncoding);

            Path relativePath = Paths.get(relativePathStr);
            Path src = srcBaseDir.resolve(relativePath);

            // 入力元ファイルの読み込み
            byte[] data = Files.readAllBytes(src);
            ByteBuffer outData = readConvertedText(
                    data, srcEncoding, destEncoding);

            Path dest;
            if (transferType == TransferType.REPLACE) {
                // 書き込み先は同一 = 上書き
                dest = src;

            } else {
                // 移動またはコピー先の出力先パスの確定
                dest = destBaseDir.resolve(relativePath);

                // 親フォルダがなければ作成する.
                Path parent = dest.getParent();
                if (!Files.isDirectory(parent)) {
                    Files.createDirectories(parent);
                }
            }

            if (reqBak) {
                // 相手先パスが既存であり、且つ、バックアップが必要な場合は
                // 拡張子を.bakとしたファイルにリネームしておく
                Path bakPath = dest.resolveSibling(
                        dest.getFileName().toString() + ".bak");
                if (Files.exists(bakPath)) {
                    Files.delete(bakPath);
                }
                Files.move(dest, bakPath);
            }

            if (transferType == TransferType.MOVE && !dest.equals(src)) {
                // 移動の場合、ソース側のファイルを削除する.
                Files.delete(src);
            }

            // ファイルの書き込み (強制上書き)
            log.info("  to " + dest + " : " + destEncoding);
            try (SeekableByteChannel outCh = Files.newByteChannel(
                    dest,
                    CREATE, TRUNCATE_EXISTING, WRITE)) {
                outCh.write(outData);
            }
        };
    }

    /**
     * 指定した文字コードでテキストファイルを読み込み、 指定した文字コードのバイト列に変換して返す.
     *
     * @param data バイトデータ
     * @param srcEncoding 読み込むエンコーディング
     * @param destEncoding バイト列に変換するエンコーディング
     * @return 変換後のバイト列
     * @throws CharacterCodingException 文字コードの変換に失敗
     */
    public final ByteBuffer readConvertedText(
            final byte[] data,
            final EncodingType srcEncoding,
            final EncodingType destEncoding
    ) throws CharacterCodingException {
        Objects.requireNonNull(data);
        Objects.requireNonNull(srcEncoding);
        Objects.requireNonNull(destEncoding);

        // 文字データの取得
        ByteBuffer byteBuf = ByteBuffer.wrap(data);
        CharBuffer charBuf = srcEncoding.decode(byteBuf);

        // エンコードしなおしてバイト列に変換
        return destEncoding.encode(charBuf);
    }
}
