package answer;

public class Answer {
    private String statusCode;
    private String id = "";

    public Answer(String statusCode, String id) {
        this.statusCode = statusCode;
        this.id = id == null ? "" : id;
    }

    public Answer(String answer) {
        String[] params = answer.split(" ");
        this.statusCode = params[0];
        if (params.length > 1) {
            this.id = params[1];
        }
    }

    @Override
    public String toString() {
        return (statusCode + " " + id).trim();
    }

    public String getStatusCode() {
        return statusCode;
    }

    public String getId() {
        return id;
    }
}
