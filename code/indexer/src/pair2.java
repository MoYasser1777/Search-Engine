import java.io.Serializable;

public class pair2 implements Serializable {
    public int position;
    public String OriginalWord;

    public pair2(int pos, String orgWord) {
        this.position = pos;
        this.OriginalWord = orgWord;
    }

    @Override
    public String toString() {
        return "{\"position\":" + position + ",\"OriginalWord\":\"" + OriginalWord + "\"}";
    }
}
