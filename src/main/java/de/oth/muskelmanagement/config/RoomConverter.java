package de.oth.muskelmanagement.config;

import de.oth.muskelmanagement.model.entity.Room;
import de.oth.muskelmanagement.repository.RoomRepository;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RoomConverter implements Converter<String, Room> {

    private final RoomRepository roomRepository;

    public RoomConverter(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public Room convert(String source) {
        if (source == null || source.isEmpty()) return null;
        try {
            Long id = Long.parseLong(source);
            return roomRepository.findById(id).orElse(null);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
