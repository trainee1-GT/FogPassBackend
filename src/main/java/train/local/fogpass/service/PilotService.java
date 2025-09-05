package train.local.fogpass.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import train.local.fogpass.entity.Pilot;
import train.local.fogpass.exception.BadRequestException;
import train.local.fogpass.exception.ResourceNotFoundException;
import train.local.fogpass.repository.PilotRepository;

import java.util.List;

@Service
@Transactional
public class PilotService {

    private final PilotRepository pilotRepository;

    public PilotService(PilotRepository pilotRepository) {
        this.pilotRepository = pilotRepository;
    }

    public List<Pilot> findAll() {
        return pilotRepository.findAll();
    }

    public Pilot findById(Long id) {
        return pilotRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pilot not found with id: " + id));
    }

    public Pilot create(Pilot pilot) {
        pilotRepository.findByEmail(pilot.getEmail()).ifPresent(p -> {
            throw new BadRequestException("Email already exists: " + p.getEmail());
        });
        return pilotRepository.save(pilot);
    }

    public Pilot update(Long id, Pilot updated) {
        Pilot existing = findById(id);
        if (!existing.getEmail().equalsIgnoreCase(updated.getEmail())) {
            pilotRepository.findByEmail(updated.getEmail()).ifPresent(p -> {
                throw new BadRequestException("Email already exists: " + p.getEmail());
            });
        }
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        return pilotRepository.save(existing);
    }

    public void delete(Long id) {
        Pilot existing = findById(id);
        pilotRepository.delete(existing);
    }
}