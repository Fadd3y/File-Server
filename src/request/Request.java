package request;

public class Request {
    private ActionTypes actionType;
    private AccessTypes accessType;
    private String fileExtension;
    private String fileName;

    public Request() {
    }

    public Request(String request) {
        String[] params = request.split(" ");
        switch (params[0]) {
            case "GET", "DELETE" -> {
                actionType = ActionTypes.valueOf(params[0]);
                accessType = AccessTypes.valueOf(params[1]);
                fileName = params[2];
                if (accessType == AccessTypes.BY_NAME && fileName.contains(".")) {
                    fileExtension = fileName.substring(fileName.lastIndexOf("."));
                }

            }
            case "PUT" -> {
                actionType = ActionTypes.valueOf(params[0]);
                fileName = params[1];
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }
            case "EXIT" -> {
                actionType = ActionTypes.valueOf(params[0].toUpperCase());
            }
        }
    }

    public void setActionType(ActionTypes actionType) {
        this.actionType = actionType;
    }

    public void setAccessType(AccessTypes accessType) {
        this.accessType = accessType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
        if (fileName.isBlank()) {
            this.fileName = "noName" + fileExtension;
        }
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension.substring(fileExtension.lastIndexOf("."));
    }

    public void setFileID(String fileID) {
        this.fileName = fileID;
    }

    public ActionTypes getActionType() {
        return actionType;
    }

    public AccessTypes getAccessType() {
        return accessType;
    }

    public String getFileName() {
        return fileName;
    }
    public int getFileID() {
        return Integer.parseInt(fileName);
    }
    public String getFileExtension() {
        return fileExtension;
    }

    @Override
    public String toString() {
        switch (actionType.toString()) {
            case "GET", "DELETE" -> {
                return String.format("%s %s %s", actionType, accessType, fileName);
            }
            case "PUT" -> {
                return  String.format("%s %s", actionType, fileName);
            }
            case "EXIT" -> {
                return actionType.toString();
            }
        }
        return "";
    }
}

