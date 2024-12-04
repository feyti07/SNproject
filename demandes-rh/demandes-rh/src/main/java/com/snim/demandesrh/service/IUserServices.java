package com.snim.demandesrh.service;

import com.snim.demandesrh.entities.User;
import com.snim.demandesrh.entities.dto.AuthenticationRequest;
import com.snim.demandesrh.entities.dto.AuthenticationResponse;
import com.snim.demandesrh.entities.dto.LightUserDto;
import com.snim.demandesrh.entities.dto.UserDto;
import com.snim.demandesrh.exceptions.InvalidTokenException;
import org.springframework.security.core.userdetails.UserDetails;

public interface IUserServices extends AbstractService<UserDto> {


    Integer validateAccount(Integer id);
    Integer invalidateAccount(Integer id);

    AuthenticationResponse register(UserDto user);


    AuthenticationResponse authenticate(AuthenticationRequest request);
    Integer update(LightUserDto userDto);

    /*void sendRegistrationConfirmationEmail(final User user);
    boolean verifyUser(final String token) throws InvalidTokenException;*/

    public void changePassword(String oldPassword, String newPassword);

    public UserDetails loadUserByUsername(String username);

}
