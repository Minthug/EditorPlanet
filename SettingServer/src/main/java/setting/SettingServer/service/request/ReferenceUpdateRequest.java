package setting.SettingServer.service.request;

public record ReferenceUpdateRequest(Long memberId, String title, String thumbnail, String videoUrl) {

    public static ReferenceUpdateRequest of(final Long memberId, final String title, final String thumbnail, final String videoUrl) {
        return new ReferenceUpdateRequest(memberId, title, thumbnail, videoUrl);
    }
}
