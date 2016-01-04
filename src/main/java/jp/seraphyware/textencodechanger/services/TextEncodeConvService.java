package jp.seraphyware.textencodechanger.services;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
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
    private static final Logger log = LoggerFactory.getLogger(TextEncodeConvService.class);

    /**
     * 文字コード一覧
     */
    private static final List<EncodingType> encodingTypes = Collections
            .unmodifiableList(Arrays.asList(EncodingType.values()));
    
    /**
     * 文字コード一覧(検査順)
     */
    private static final List<EncodingType> checkEncodingOrder = encodingTypes
            .stream().sorted((a, b) -> {
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

    /**
     * 文字コード一覧を返す.
     *
     * @return 対象とする文字コードのリスト
     */
    public List<EncodingType> getEncodings() {
        return encodingTypes;
    }
    
    /**
     * 文字コード検査順序での文字コードのリストを返す.
     * @return  検査順序の文字コードのリスト
     */
    public List<EncodingType> getCheckEncodingOrder() {
        return checkEncodingOrder;
    }

    /**
     * 指定したバイトバッファ内の文字コードを推定する. <br>
     * 不明な場合はnullを返す.<br>
     *
     * @param byteBuf バイトバッファ
     * @return 文字コード
     * @throws IOException 失敗
     */
    public EncodingType presumeEncoding(
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
                if (log.isDebugEnabled()) {
                    log.debug("coding error: " + enc);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("-" + encodingOk);
        }
        return encodingOk;
    }

    /**
     * テキストの読み込み.
     * @param byteBuf バイトデータ(リワインドされる)
     * @param srcEncoding 文字コード
     * @return 変換されたテキスト
     * @throws CharacterCodingException 読み込みに失敗
     */
    public CharBuffer readText(
            final ByteBuffer byteBuf,
            final EncodingType srcEncoding
    ) throws CharacterCodingException {
        Objects.requireNonNull(byteBuf);
        Objects.requireNonNull(srcEncoding);

        byteBuf.rewind();
        return srcEncoding.decode(byteBuf);
    }
    
    /**
     * テキストの書き込み
     * @param charBuf 書き込むテキスト
     * @param destEncoding 文字コード
     * @return 変換されたバイトバッファ
     * @throws CharacterCodingException 書き込みに失敗
     */
    public ByteBuffer writeBytes(
            final CharBuffer charBuf,
            final EncodingType destEncoding
    ) throws CharacterCodingException {
        Objects.requireNonNull(charBuf);
        Objects.requireNonNull(destEncoding);
        
        return destEncoding.encode(charBuf);
    }

    /**
     * 指定した文字コードでテキストファイルを読み込み、 指定した文字コードのバイト列に変換して返す.
     *
     * @param byteBuf バイトデータ
     * @param srcEncoding 読み込むエンコーディング
     * @param destEncoding バイト列に変換するエンコーディング
     * @return 変換後のバイト列
     * @throws CharacterCodingException 文字コードの変換に失敗
     */
    public ByteBuffer readConvertedText(
            final ByteBuffer byteBuf,
            final EncodingType srcEncoding,
            final EncodingType destEncoding
    ) throws CharacterCodingException {
        Objects.requireNonNull(byteBuf);
        Objects.requireNonNull(srcEncoding);
        Objects.requireNonNull(destEncoding);

        byteBuf.rewind();
        return writeBytes(readText(byteBuf, srcEncoding), destEncoding);
    }
}
