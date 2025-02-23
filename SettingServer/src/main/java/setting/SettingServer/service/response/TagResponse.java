package setting.SettingServer.service.response;

import setting.SettingServer.entity.Tag;

public record TagResponse(Long id, String name, int useCount) {

    public static TagResponse from(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getUseCount()
        );
    }
}
