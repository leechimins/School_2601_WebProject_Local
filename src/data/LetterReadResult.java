package data;

public class LetterReadResult {
    private final String content;
    private final boolean isSignatureVerified;

    public LetterReadResult(String content, boolean isSignatureVerified) {
        this.content = content;
        this.isSignatureVerified = isSignatureVerified;
    }

    public String getContent() {
        return content;
    }

    public boolean isSignatureVerified() {
        return isSignatureVerified;
    }
}
