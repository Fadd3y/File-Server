package request;

public enum AccessTypes {
    BY_NAME,
    BY_ID;

    public static AccessTypes setByTypeId(int n) {
        return AccessTypes.values()[n];
    }
}
