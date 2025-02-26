package AspirationAlley.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import AspirationAlley.model.Post;
import AspirationAlley.model.User;

public interface PostRepository extends JpaRepository<Post, Long> {
	List<Post> findByAuthor_Id(Long userId);
}

