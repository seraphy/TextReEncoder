package jp.seraphyware.textencodechanger.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ファイルの差し替え処理を行うサービス.<br>
 * 
 * @author seraphy
 */
@Component
public class FileReplaceService {
    
    /**
     * ロガー.
     */
    private static final Logger log = LoggerFactory.getLogger(FileReplaceService.class);

    /**
     * バイト列を受け取りテキストとして読み込む.
     */
    @FunctionalInterface
    public interface ContentReader {
        
        CharBuffer read(byte[] data) throws CharacterCodingException;
    }
    
    /**
     * テキストを変換してバイト列として返す.
     */
    @FunctionalInterface
    public interface ContentConverter {
        
        ByteBuffer convert(CharBuffer inp) throws CharacterCodingException;
    }

    /**
     * テキストファイルの変換を行う
     */
    @FunctionalInterface
    public interface FileContentConverter {
        
        /**
         * 相対パスと文字読み込み方法を指定して対象コンテンツを読み取り変換する.<br>
         * @param relativeFileName 相対パス
         * @param reader ファイルの読み込み方法
         * @return 変換結果、変換されればtrue
         * @throws IOException 
         */
        boolean convert(String relativeFileName, ContentReader reader) throws IOException;
    }
    
    /**
     * テキストの変換を行うコンバータを作成して返すファクトリ.
     *
     * @param srcDir 入力元ディレクトリ
     * @param destDir 出力先ディレクトリ
     * @param transferType 転送モード
     * @param overwriteMode 上書きモード
     * @param converter コンテンツコンバータ 
     * @return コンバータ
     */
    public FileContentConverter createFileContentConverter(
            final String srcDir,
            final String destDir,
            final TransferType transferType,
            final OverwriteMode overwriteMode,
            final ContentConverter converter
    ) {
        Objects.requireNonNull(srcDir);
        Objects.requireNonNull(transferType);
        Objects.requireNonNull(converter);

        Path srcBaseDir = Paths.get(srcDir);
        Path destBaseDir;
        if (destDir != null && transferType != TransferType.REPLACE) {
            destBaseDir = Paths.get(destDir);
        } else {
            destBaseDir = srcBaseDir;
        }

        return (relativePathStr, reader) -> {
            Objects.requireNonNull(relativePathStr);
            Objects.requireNonNull(reader);
            log.info("convert from " + relativePathStr);

            Path relativePath = Paths.get(relativePathStr);
            Path src = srcBaseDir.resolve(relativePath);

            // 入力元ファイルの読み込み
            byte[] data = Files.readAllBytes(src);
            CharBuffer inp = reader.read(data);
            ByteBuffer outData = converter.convert(inp);

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

            if (Files.exists(dest)) {
                if (overwriteMode == OverwriteMode.CREATE_BACKUP) {
                    // 相手先パスが既存であり、且つ、バックアップが必要な場合は
                    // 拡張子を.bakとしたファイルにリネームしておく
                    Path bakPath = dest.resolveSibling(
                            dest.getFileName().toString() + ".bak");
                    if (Files.exists(bakPath)) {
                        Files.delete(bakPath);
                    }
                    Files.move(dest, bakPath);

                } else if (overwriteMode == OverwriteMode.SKIP) {
                    // 何もせずスキップする.
                    return false;
                }
            }

            if (transferType == TransferType.MOVE && !dest.equals(src)) {
                // 移動の場合、ソース側のファイルを削除する.
                Files.delete(src);
            }

            // ファイルの書き込み (強制上書き)
            log.info("  to " + dest);
            try (SeekableByteChannel outCh = Files.newByteChannel(
                    dest,
                    CREATE, TRUNCATE_EXISTING, WRITE)) {
                outCh.write(outData);
            }
            return true;
        };
    }
}
