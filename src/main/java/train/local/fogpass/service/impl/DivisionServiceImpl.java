package train.local.fogpass.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import train.local.fogpass.entity.Division;
import train.local.fogpass.repository.DivisionRepository;
import train.local.fogpass.service.DivisionService;

import java.util.List;

@Service
public class DivisionServiceImpl implements DivisionService {

    private final DivisionRepository divisionRepository;

    // Explicit constructor for constructor injection (avoids Lombok dependency issues)
    public DivisionServiceImpl(DivisionRepository divisionRepository) {
        this.divisionRepository = divisionRepository;
    }

    @Override
    public List<Division> getAllDivisions() {
        return divisionRepository.findAll();
    }

    @Override
    public List<Division> getDivisionsByZone(Long zoneId) {
        return divisionRepository.findByZoneZoneId(zoneId);
    }

    @Override
    public Division saveDivision(Division division) {
        return divisionRepository.save(division);
    }

    @Override
    public List<Division> saveAllDivisions(List<Division> divisions) {
        return divisionRepository.saveAll(divisions);
    }

    @Override
    public Division getDivisionById(Long id) {
        return divisionRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteDivision(Long id) {
        divisionRepository.deleteById(id);
    }
}
