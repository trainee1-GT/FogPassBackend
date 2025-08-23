package train.local.fogpass.service;

import train.local.fogpass.entity.Division;
import java.util.List;

public interface DivisionService {
    List<Division> getAllDivisions();
    List<Division> getDivisionsByZone(Long zoneId);
    Division saveDivision(Division division);
    List<Division> saveAllDivisions(List<Division> divisions);
    Division getDivisionById(Long id);
    void deleteDivision(Long id);
}
