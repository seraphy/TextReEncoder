package jp.seraphyware.textencodechanger.services;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ファイル検索サービス.
 *
 * @author seraphy
 */
@Component
public class FileWalkService {

    /**
     * ロガー.
     */
    private static final Logger log = LoggerFactory.getLogger(FileWalkService.class);

    /**
     * テキストの文字コード変換のサービス.
     */
    @Autowired(required = true)
    private TextEncodeConvService encConvSrv;

    /**
     * 行末タイプの変換サービス.
     */
    @Autowired(required = true)
    private TextTermConvService termConvSrv;

    /**
     * ファイルのパターンマッチ文字列から正規表現のパターンをリストとして返す.
     * ファイルのパターンは0文字以上の任意を「*」、任意の一文字を「?」として、 複数のパターンはセミコロンによって区切ります.
     * 前後の空白はパターンとして無視されます. 大文字小文字の違いは無視されます.
     *
     * @param patterns ファイルのパターンマッチ、セミコロン区切り
     * @return 正規表現のパターンリスト
     */
    public final List<Pattern> makePatterns(final String patterns) {
        Objects.requireNonNull(patterns);

        ArrayList<Pattern> regexps = new ArrayList<>();
        for (String pattern : patterns.split(";")) {
            pattern = pattern.trim();
            if (pattern.isEmpty()) {
                continue;
            }
            if (pattern.equals("*.*")) {
                pattern = "*";
            }
            String regexpStr = "^" + pattern.replace(".", "\\.").
                    replace("?", ".").replace("*", ".*") + "$";
            regexps.add(Pattern.compile(regexpStr, Pattern.CASE_INSENSITIVE));
        }

        log.debug("makePatterns=" + regexps);
        return regexps;
    }

    /**
     * ファイル名のマッチングオブジェクト.
     */
    @FunctionalInterface
    public interface FileNameMatcher {

        /**
         * パス(名前部)とマッチングを行う.
         *
         * @param name 名前
         * @return マッチしたか？
         */
        boolean match(Path name);
    }

    /**
     * ファイル名からパターンマッチするマッチャーのファクトリ.
     *
     * @param patterns パターン
     * @return ファイル名のマッチングオブジェクト
     */
    public FileNameMatcher createFileNameMatcher(List<Pattern> patterns) {
        Objects.requireNonNull(patterns);

        return (name) -> patterns.stream().anyMatch(
                pattern -> pattern.matcher(name.toString()).matches());
    }

    /**
     * ファイルツリーを走査します.
     *
     * @param srcDir 入力元フォルダ
     * @param recursive 再帰的にサブフォルダを検査するか？
     * @param fileNameMatcher ファイル名のマッチャー
     * @param predicate 停止条件の判定
     * @return マッチしたファイルリスト
     */
    private List<FileInfo> walkFiles(
            final Path srcDir,
            final boolean recursive,
            final FileNameMatcher fileNameMatcher,
            final BiPredicate<Path, BasicFileAttributes> predicate
    ) {
        ArrayList<FileInfo> files = new ArrayList<>();
        try {
            int limit;
            if (recursive) {
                limit = Integer.MAX_VALUE;
            } else {
                limit = 1;
            }

            SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(
                        final Path filePath,
                        final BasicFileAttributes attrs
                ) throws IOException {
                    Path name = filePath.getFileName();
                    if (!attrs.isDirectory()) {
                        if (fileNameMatcher.match(name)) {
                            // ファイルの読み取り
                            log.debug("load: " + filePath);
                            byte[] data = Files.readAllBytes(filePath);
                            ByteBuffer byteBuf = ByteBuffer.wrap(data);

                            // 文字コードの推定
                            EncodingType enc = encConvSrv
                                    .presumeEncoding(byteBuf);

                            // 行末タイプの推定
                            TextTermType term = TextTermType.UNKNOWN;
                            if (enc != null) {
                                try {
                                    CharBuffer charBuf = encConvSrv.readText(
                                            data, enc);
                                    term = termConvSrv.presumeTermType(charBuf);
                                } catch (IOException ex) {
                                    log.warn("can't decode text." + filePath +
                                            "|enc=" + enc);
                                }
                            }

                            files.add(new FileInfo(filePath, enc, term));
                        }
                        if (!predicate.test(filePath, attrs)) {
                            return FileVisitResult.TERMINATE;
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(
                        final Path dir,
                        final BasicFileAttributes attrs
                ) throws IOException {
                    if (!predicate.test(dir, attrs)) {
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
            };
            
            Files.walkFileTree(
                    srcDir,
                    EnumSet.noneOf(FileVisitOption.class),
                    limit,
                    visitor);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return files;
    }

    /**
     * ファイル情報.
     */
    public static final class FileInfo {

        /**
         * パス.
         */
        private final Path path;

        /**
         * 文字コード.
         */
        private final EncodingType encoding;

        /**
         * 行末タイプ.
         */
        private final TextTermType termType;

        /**
         * コンストラクタ.
         *
         * @param path パス
         * @param encoding 文字コード
         * @param termType 行末タイプ
         */
        @SuppressWarnings("checkstyle:hiddenfield")
        public FileInfo(final Path path, final EncodingType encoding,
                final TextTermType termType) {
            Objects.requireNonNull(path);

            this.path = path;
            this.encoding = encoding;
            this.termType = termType;
        }

        /**
         * パス.
         *
         * @return パス
         */
        public Path getPath() {
            return path;
        }

        /**
         * 文字コード.
         *
         * @return 文字コード.
         */
        public EncodingType getEncoding() {
            return encoding;
        }

        /**
         * 行末タイプ.
         * 
         * @return  行末タイプ
         */
        public TextTermType getTermType() {
            return termType;
        }

        /**
         * 診断文字列を返す.
         *
         * @return 診断文字列
         */
        @Override
        public String toString() {
            return encoding + "=" + path;
        }
    }

    /**
     * 指定したパスの走査を行うタスク生成して返します. (まだ実行はされていません.)
     *
     * @param dir 対象ディレクトリ
     * @param recursive 再帰的に検査するか？
     * @param regexps マッチする名前のパターンリスト
     * @return タスク
     */
    public final FileWalkerCallable createCallable(
            final Path dir,
            final boolean recursive,
            final List<Pattern> regexps
    ) {
        Objects.requireNonNull(dir);
        Objects.requireNonNull(regexps);
        return new FileWalkerCallable() {

            /**
             * 通知を受けるコールバック
             */
            private ProgressCallback progressCallback;

            /**
             * 進行状況の通知を受けるコールバックの設定.
             *
             * @param callback コールバック、不要ならnull可
             */
            @Override
            public void setProgressCallback(
                    final ProgressCallback callback
            ) {
                this.progressCallback = callback;
            }

            /**
             * 進行状況の通知を受けるコールバックの取得.
             *
             * @return コールバック、未設定ならnull
             */
            @Override
            public ProgressCallback getProgressCallback() {
                return progressCallback;
            }

            /**
             * ワーカーの実行
             *
             * @return ファイルリスト
             * @throws Exception 何らかの失敗
             */
            @Override
            public List<FileInfo> call() throws Exception {
                log.info("★begin worker");
                updateTitle("searching...");
                List<FileInfo> files = walkFiles(
                        dir,
                        recursive,
                        createFileNameMatcher(regexps),
                        (path, attr) -> {
                            if (attr.isDirectory()) {
                                // 走査中の相対ディレクトリの表示
                                Path relative = dir.relativize(path);
                                updateMessage(relative.toString());
                            }
                            // タスクがキャンセルされていれば走査は非継続とする.
                            return !isCancelled();
                        });
                log.info("★end worker");
                return files;
            }

            private void updateMessage(final String message) {
                if (progressCallback != null) {
                    progressCallback.updateMessage(message);
                }
            }

            private void updateTitle(final String title) {
                if (progressCallback != null) {
                    progressCallback.updateTitle(title);
                }
            }

            private boolean isCancelled() {
                return Thread.currentThread().isInterrupted();
            }
        };
    }
}
