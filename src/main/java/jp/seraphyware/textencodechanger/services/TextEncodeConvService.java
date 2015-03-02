package jp.seraphyware.textencodechanger.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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
     * 変換する文字コード一覧.
     */
    private static final String[] ENCODINS = {
        "UTF-8",
        "Windows-31j",
        "EUC_JP"
    };

    /**
     * 文字コード一覧を返す.
     *
     * @return 対象とする文字コードのリスト
     */
    public final List<String> getEncodings() {
        return Arrays.asList(ENCODINS);
    }

    /**
     * 指定したファイルの文字コードを推定する. 不明な場合はnullを返す.
     *
     * @param file ファイル
     * @return 文字コード
     * @throws IOException 失敗
     */
    public final String presumeEncoding(final Path file) throws IOException {
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
    public final String presumeEncoding(
            final ByteBuffer byteBuf
    ) throws IOException {
        Objects.requireNonNull(byteBuf);

        String encodingOk = null;
        for (String encStr : ENCODINS) {
            Charset cs = Charset.forName(encStr);
            CharsetDecoder dec = cs.newDecoder();
            dec.onUnmappableCharacter(CodingErrorAction.REPORT);
            dec.onMalformedInput(CodingErrorAction.REPORT);

            try {
                // 再読み込みのために位置を巻き戻す
                byteBuf.rewind();

                // 読み込みを試行する.
                dec.decode(byteBuf);
                encodingOk = encStr;
                break;

            } catch (CharacterCodingException ex) {
                // 変換エラーが生じた場合
                log.info("coding error: " + ex);
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
                Charset srcEncoding
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
            final Charset destEncoding
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
            final Charset srcEncoding,
            final Charset destEncoding
    ) throws CharacterCodingException {
        Objects.requireNonNull(data);
        Objects.requireNonNull(srcEncoding);
        Objects.requireNonNull(destEncoding);

        // 文字データの取得
        ByteBuffer byteBuf = ByteBuffer.wrap(data);
        CharsetDecoder dec = srcEncoding.newDecoder();
        CharBuffer charBuf = dec.decode(byteBuf);

        // エンコードしなおしてバイト列に変換
        CharsetEncoder enc = destEncoding.newEncoder();
        ByteBuffer outData = enc.encode(charBuf);
        return outData;
    }
}
