package jp.seraphyware.textencodechanger.services;

/**
 * 行端タイプ.
 * 
 * @author seraphy
 */
public enum TextTermType {
    CRLF("\r\n"),
    LF("\n"),
    CR("\r"),
    UNKNOWN(null);
    
    private final String chars;
    
    TextTermType(String chars) {
        this.chars = chars;
    }
    
    public String getChars() {
        return chars;
    }
}
