package jp.seraphyware.textencodechanger.services;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

/**
 * サポートされるキャラクターセット.<br>
 * (表示順)
 * 
 * @author seraphy
 */
public enum EncodingType {
    UTF8(Charset.forName("UTF-8"), null),
    UTF8_BOM(Charset.forName("UTF-8"), new byte[]{(byte) 0xef, (byte) 0xbb, (byte) 0xbf}),
    Windows31J(Charset.forName("Windows-31j"), null),
    EUC_JP(Charset.forName("EUC_JP"), null),
    UTF16_BOM_LE(Charset.forName("UTF-16LE"), new byte[]{(byte) 0xff, (byte) 0xfe}),
    UTF16_BOM_BE(Charset.forName("UTF-16BE"), new byte[]{(byte) 0xfe, (byte) 0xff}),
    UTF16_LE(Charset.forName("UTF-16LE"), null), // なんでも合致しやすく推定は困難
    UTF16_BE(Charset.forName("UTF-16BE"), null); // なんでも合致しやすく推定は困難

    /**
     * 文字コード
     */
    private final Charset cs;
    
    /**
     * BOMの定義、なければnull
     */
    private final byte[] bom;
    
    /**
     * コンストラクタ
     * @param cs 文字コード
     * @param bom BOMの定義、なければnull
     */
    EncodingType(Charset cs, byte[] bom) {
        this.cs = cs;
        this.bom = bom;
    }
    
    /**
     * 文字コード
     * @return 
     */
    public Charset getCharset() {
        return cs;
    }
    
    /**
     * 必要とされるBOM.<br>
     * 定義されていない場合はnull.<br>
     * @return 
     */
    public byte[] getBOM() {
        byte[] tmp = new byte[bom.length];
        System.arraycopy(bom, 0, tmp, 0, bom.length);
        return tmp;
    }
    
    /**
     * BOMの長さを返す.<br>
     * BOMがない場合は0を返す.<br>
     * @return 
     */
    public int getBOMLength() {
        return bom == null ? 0 : bom.length;
    }
    
    /**
     * BOMの一致を判定する.
     * @param buf
     * @param pos
     * @param len
     * @return 
     */
    public boolean isSameBOM(byte[] buf, int pos, int len) {
        if (bom == null || buf == null) {
            return false;
        }
        if (len < bom.length) {
            // BOMに満たない
            return false;
        }
        
        for (int idx = 0; idx < bom.length; idx++) {
            byte a = bom[idx];
            byte b = buf[pos + idx];
            if (a != b) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * BOMを判定する
     * @param buf
     * @return 
     */
    public boolean checkBOM(ByteBuffer buf) {
        int len = getBOMLength();
        if (len == 0) {
            return true;
        }
        byte[] tmp = new byte[len];
        buf.get(tmp);
        return isSameBOM(tmp, 0, len);
    }
    
    /**
     * この文字コードで変換可能なバイト列であるか検証する.<br>
     * BOMが必要な文字コードの場合はBOMの有無もチェックする.<br>
     * @param byteBuf
     * @return 
     */
    public boolean checkEncodable(ByteBuffer byteBuf) {
        CharsetDecoder dec = cs.newDecoder();
        dec.onUnmappableCharacter(CodingErrorAction.REPORT);
        dec.onMalformedInput(CodingErrorAction.REPORT);

        // BOMの読み取り(もしくはスキップ)
        if (checkBOM(byteBuf)) {
            // BOMの読み取り成功の場合
            try {
                // 読み込みを試行する.
                dec.decode(byteBuf);
                return true;

            } catch (CharacterCodingException ex) {
                // 無視する.
            }
        }
        return false;
    }
    
    /**
     * バイト列を読み込み、この文字コードとして変換した文字バッファとして返す.<br>
     * BOMが必要な場合でBOMがない場合、もしくは文字コードが変換できない場合はエラーを返す.<br>
     * @param byteBuf
     * @return
     * @throws CharacterCodingException 
     */
    public CharBuffer decode(ByteBuffer byteBuf) throws CharacterCodingException {
        // BOMの分を読み飛ばす
        if (!checkBOM(byteBuf)) {
            // BOMの不一致
            throw new CharacterCodingException();
        }
        CharsetDecoder dec = cs.newDecoder();
        return dec.decode(byteBuf);
    }

    /**
     * 文字列バッファを読み込んで、この文字コードで変換されたバイト列を返す.<br>
     * 必要であれば、先頭にBOMが付与される.<br>
     * @param charBuf
     * @return
     * @throws CharacterCodingException 
     */
    public ByteBuffer encode(CharBuffer charBuf) throws CharacterCodingException {
        CharsetEncoder enc = cs.newEncoder();
        ByteBuffer contentBuf = enc.encode(charBuf);
        
        int len = getBOMLength();
        if (len == 0) {
            // BOMなし
            return contentBuf;
        }

        // BOMありの場合はBOMをつけたバッファを返す.
        byte[] tmp = new byte[len + contentBuf.limit()];
        ByteBuffer resultBuf = ByteBuffer.wrap(tmp);
        
        resultBuf.put(bom);
        resultBuf.put(contentBuf);
        resultBuf.flip();
        return resultBuf;
    }
}
