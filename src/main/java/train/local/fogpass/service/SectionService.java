package train.local.fogpass.service;

import train.local.fogpass.entity.Section;

import java.util.List;

public interface SectionService {
    List<Section> getAllSections();
    List<Section> getSectionsByDivision(Long divisionId);
    Section saveSection(Section section);
    List<Section> saveAllSections(List<Section> sections);
    void deleteSection(Long id);
}