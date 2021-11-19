package main.repositories;

import main.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    Tag findByNameEquals(String name);
    List<Tag> findTagsByNameContaining(String tagName);

}
