package setting.SettingServer.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import setting.SettingServer.entity.Tag;
import setting.SettingServer.repository.TagRepository;
import setting.SettingServer.service.response.TagResponse;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    @Transactional
    public Tag createTag(String name) {
        return tagRepository.findByName(name)
                .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
    }

    public List<TagResponse> searchTags(String keyword) {
        return tagRepository.findByNameContaining(keyword)
                .stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    public List<TagResponse> getPopularTags() {
        return tagRepository.findTop10ByOrderByUseCountDesc()
                .stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());

    }
}
