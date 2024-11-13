package request;

public enum ActionTypes {
    GET,
    PUT,
    DELETE,
    EXIT;

    public static ActionTypes setByTypeId(int n) {
        return ActionTypes.values()[n];
    }
}
