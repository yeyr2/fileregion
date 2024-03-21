package message;

public class ExceptionMessage extends Message {

    public static final String ExceptionFile = "ExceptionFile";
    public static final String ExceptionString = "ExceptionString";

    String cause = null;
    String ExceptionType = "NULL";

    public ExceptionMessage() {}
    public ExceptionMessage(String cause) {
        this.cause = cause;
    }

    @Override
    public int getType() {
        return ExceptionMessage;
    }

    public String getCause() {
        return cause;
    }

    public ExceptionMessage setCause(String cause) {
        this.cause = cause;
        return this;
    }

    public String getExceptionType() {
        return ExceptionType;
    }

    public ExceptionMessage setExceptionType(String cause,String exceptionType) {
        this.cause = cause;
        ExceptionType = exceptionType;
        return this;
    }
}
