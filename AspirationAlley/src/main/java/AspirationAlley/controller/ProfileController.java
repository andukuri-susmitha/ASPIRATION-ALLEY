package AspirationAlley.controller;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import AspirationAlley.model.Comment;
import AspirationAlley.model.Image;
import AspirationAlley.model.Post;
import AspirationAlley.model.User;
import AspirationAlley.repository.CommentRepository;
import AspirationAlley.repository.PostRepository;
import AspirationAlley.repository.UserRepository;
import AspirationAlley.service.CommentService;
import AspirationAlley.service.ImageService;
import AspirationAlley.service.PostService;
import AspirationAlley.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;


@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostService postService;
    @Autowired
    private UserService userService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private CommentService commentService;

    // Helper method to set common profile data
    private void setProfileData(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        Image logoImage = imageService.getImageById(7);
        String base64Logo = Base64.getEncoder().encodeToString(logoImage.getImage());
        model.addAttribute("logoImageBase64", base64Logo);

        Image profileImage = imageService.getImageById(6);
        String base64Profile = Base64.getEncoder().encodeToString(profileImage.getImage());
        model.addAttribute("profileImageBase64", base64Profile);

        if (userId == null) {
            model.addAttribute("isLoggedIn", false);
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
        	Map<Long, List<Comment>> postComments = new HashMap<>();
            List<Post> posts = postService.getPostsByUserId(userId);
            Collections.reverse(posts);
            for (Post post : posts) {
            	List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(post.getId());
                Collections.reverse(comments);
                postComments.put(post.getId(), comments);
                User author = post.getAuthor();
                if (author != null) {
                    String profileBase64 = author.getProfilePicture() != null ? Base64.getEncoder().encodeToString(author.getProfilePicture()) : base64Profile;
                    post.setProfileBase64(profileBase64); // Set profile picture for this post
                }
            }

            List<Boolean> activeStreak = userService.calculateActiveStreak(user);
            List<String> streakDates = userService.getStreakDates();

            model.addAttribute("activeStreak", activeStreak);
            model.addAttribute("streakDates", streakDates);
            model.addAttribute("posts", posts);
            model.addAttribute("postComments", postComments);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("user", user);
        }
    }

    @GetMapping("/profile")
    public String profilePage(Model model, HttpSession session) {
        // Call the helper method to set profile data
        setProfileData(model, session);
        return "profile"; // Assuming your profile page is named 'profile.html'
    }

    @PostMapping("/profile")
    public String updateProfile(
            @RequestParam Long userId,
            @RequestParam String email,
            @RequestParam String phoneNumber,
            Model model,
            HttpSession session) {

        System.out.println("Received data: userId=" + userId + ", email=" + email + ", phoneNumber=" + phoneNumber);

        boolean updateSuccess = userService.updateUserProfile(userId, email, phoneNumber);

        if (updateSuccess) {
            model.addAttribute("message", "Profile updated successfully");
        } else {
            model.addAttribute("message", "Failed to update due to wrong mail or phn number");
        }

        // After update, call the helper method to refresh the profile data
        setProfileData(model, session);

        return "profile"; // Return the same profile page after updating
    }
    @PostMapping("/change-password")
    public String updatePassword(@RequestParam Long userId, 
            @RequestParam String currentPassword, 
            @RequestParam String newPassword, 
            @RequestParam String confirmNewPassword,
    		Model model,
            HttpSession session
            ) {
        
        // Update password logic
        boolean updateSuccess=userService.changePassword(userId, currentPassword, newPassword);
        if (updateSuccess) {
            model.addAttribute("message", "Password updated successfully.");
        } else {
            model.addAttribute("message", "Failed to update the password because the current password is incorrect.");
        }

        // After update, call the helper method to refresh the profile data
        setProfileData(model, session);

        return "profile";
    }
    @PostMapping("/updateProfilePicture")
    public String updateProfilePicture(
            @RequestParam Long userId,
            @RequestParam("profileImage") MultipartFile profileImage,
            Model model, HttpSession session) {
        try {
            // Fetch the user by ID
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                model.addAttribute("message", "User not found.");
                setProfileData(model, session);
                return "redirect:/profile";
            }

            // Check if the file is uploaded
            if (!profileImage.isEmpty()) {
                // Validate the image size (example: max 500KB)
                final long MAX_IMAGE_SIZE = 500 * 1024; 
//                System.out.println(profileImage.getSize());
                if (profileImage.getSize() > 100000) {
                	setProfileData(model, session);
                    model.addAttribute("message", "Failed to update: Image size exceeds 100KB.");
                    
                    return "redirect:/profile";
                }

                // Update profile picture
                user.setProfilePicture(profileImage.getBytes());
                userRepository.save(user);

                model.addAttribute("message", "Profile picture updated successfully.");
            } else {
                model.addAttribute("message", "Please upload a valid profile picture.");
            }

            // Add user details back to the model
            model.addAttribute("user", user);

        } catch (IOException e) {
            model.addAttribute("message", "An error occurred while processing the profile picture.");
            e.printStackTrace();
        }
        return "redirect:/profile";
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id, HttpSession session) {
//    	System.out.println("cameeeeeeeeeeeeeeeeee!!!!!!!!!!!!");
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Please log in to delete a post.");
        }

        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        boolean deleted = postService.deletePostById(id);
        if (deleted) {
            return ResponseEntity.ok("Post deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Post not found.");
        }
    }
    @PostMapping("/reportPost/{postId}")
    public ResponseEntity<Map<String, String>> reportPost(@PathVariable Long postId, @RequestParam String userId) {
        Map<String, String> response = new HashMap<>();
        boolean isReported = true; // Your reporting service logic
        
        if (isReported) {
            response.put("success", "true");
            response.put("message", "Post " + postId + " reported by user " + userId);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", "false");
            response.put("message", "Failed to report post " + postId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }




}
