package train.local.fogpass.service.impl;

import org.springframework.stereotype.Service;
import train.local.fogpass.entity.Section;
import train.local.fogpass.repository.SectionRepository;
import train.local.fogpass.service.SectionService;

import java.util.List;

@Service
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;

    public SectionServiceImpl(SectionRepository sectionRepository) {
        this.sectionRepository = sectionRepository;
    }

    @Override
    public List<Section> getAllSections() {
        return sectionRepository.findAll();
    }

    @Override
    public List<Section> getSectionsByDivision(Long divisionId) {
        return sectionRepository.findByDivision_Id(divisionId);
    }

    @Override
    public Section saveSection(Section section) {
        return sectionRepository.save(section);
    }

    @Override
    public List<Section> saveAllSections(List<Section> sections) {
        return sectionRepository.saveAll(sections);
    }

    @Override
    public void deleteSection(Long id) {
        sectionRepository.deleteById(id);
    }
}