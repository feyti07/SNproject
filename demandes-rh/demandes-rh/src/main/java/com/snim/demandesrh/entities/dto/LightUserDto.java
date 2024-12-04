package com.snim.demandesrh.entities.dto;


import com.snim.demandesrh.entities.User;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Ali Bouali
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class LightUserDto {

    private Integer id;

    @NotNull(message = "Le prenom ne doit pas etre vide")
    @NotEmpty(message = "Le prenom ne doit pas etre vide")
    @NotBlank(message = "Le prenom ne doit pas etre vide")
    private String username;


    public static LightUserDto fromEntity(User user) {
        // null check
        return LightUserDto.builder()
                .username(user.getUsername())
                .build();
    }


   public static User toEntity(LightUserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        return user;
    }


    /*
    public static User toEntity(LightUserDto user) {
    return User.builder()
        .username(user.getUsername)
        .build();
  }

    * */


}

