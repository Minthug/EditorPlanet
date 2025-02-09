package setting.SettingServer.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class GcpStorageFile {

    private String originalFilename;
    private String uploadFilename;
    private String uploadFilePath;
    private String uploadFileUrl;
    private boolean uploadSuccessful;
    private String errorMessage;

    public boolean isUploadSuccessful() {
        return uploadSuccessful;
    }

    public void setUploadSuccessful(boolean uploadSuccessful) {
        this.uploadSuccessful = uploadSuccessful;
    }
}
