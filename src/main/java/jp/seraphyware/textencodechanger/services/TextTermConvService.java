package jp.seraphyware.textencodechanger.services;

import java.nio.CharBuffer;
import java.util.Objects;
import java.util.function.IntConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * テキストの行端変換サービス.
 *
 * @author seraphy
 */
@Component
public class TextTermConvService {

    /**
     * ロガー.
     */
    private static final Logger log = LoggerFactory.getLogger(TextEncodeConvService.class);

    /**
     * (内部状態用.)
     */
    private enum ScanState {
        NORMAL,
        CR
    }
    
    /**
     * テキストの改行コードを推定する. <br>
     * 不明な場合はnullを返す.<br>
     *
     * @param buf バイトバッファ
     * @return 終端タイプ
     */
    public TextTermType presumeTermType(CharBuffer buf) {
        return presumeTermType(buf, i ->  {});
    }

    /**
     * テキストの改行コードを推定する. <br>
     * 不明な場合はnullを返す.<br>
     * 文字および行末をreceiverに通知する.<br>
     *
     * @param buf バイトバッファ
     * @param receiver 文字受け取り、-1で行末
     * @return 終端タイプ
     */
    public TextTermType presumeTermType(CharBuffer buf,
            IntConsumer receiver) {
        Objects.requireNonNull(buf);

        ScanState mode = ScanState.NORMAL;
        
        int countOfCr = 0;
        int countOfLf = 0;
        int countOfCrLf = 0;
        
        // 再読み込みのために位置を巻き戻す
        buf.rewind();

        // バッファを走査する.
        while (buf.position() < buf.limit()) {
            char ch = buf.get();
            if (mode == ScanState.NORMAL) {
                if (ch == '\r') {
                    mode = ScanState.CR;

                } else if (ch == '\n') {
                    countOfLf++;
                    receiver.accept(-1);
                    mode = ScanState.NORMAL;

                } else {
                    receiver.accept(ch);
                }

            } else if (mode == ScanState.CR) {
                if (ch == '\n') {
                    countOfCrLf++;
                    receiver.accept(-1);

                } else {
                    countOfCr++;
                    receiver.accept(-1);
                    receiver.accept(ch);
                }
                mode = ScanState.NORMAL;
            }
        }
        if (mode == ScanState.CR) {
            countOfCr++;
            receiver.accept(-1);
        }

        if (countOfCr > 0 && countOfCrLf == 0 && countOfLf == 0) {
            return TextTermType.CR;
        }
        if (countOfCr == 0 && countOfCrLf > 0 && countOfLf == 0) {
            return TextTermType.CRLF;
        }
        if (countOfCr == 0 && countOfCrLf == 0 && countOfLf > 0) {
            return TextTermType.LF;
        }
        
        log.debug("result: cr=" + countOfCr + "/crlf=" + countOfCrLf + "/lf=" +
                countOfLf);
        
        return TextTermType.UNKNOWN;
    }
    
    /**
     * テキストの改行コードを変更する.
     * @param charBuf 対象となるテキスト
     * @param termType 改行コード
     * @return 
     */
    public CharBuffer changeTermType(CharBuffer charBuf,
            TextTermType termType) {
        Objects.requireNonNull(charBuf);
        Objects.requireNonNull(termType);
        
        if (termType == TextTermType.UNKNOWN) {
            // 何もしない.
            return charBuf;
        }

        StringBuilder buf = new StringBuilder();
        presumeTermType(charBuf, ch -> {
            if (ch == -1) {
                buf.append(termType.getChars());
            } else {
                buf.append((char) ch);
            }
        });
        return CharBuffer.wrap(buf);
    }
}
