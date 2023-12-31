package com.example.borys_mankowski_test_10.appuser;

import com.example.borys_mankowski_test_10.appuser.model.AppUser;
import com.example.borys_mankowski_test_10.appuser.model.AppUserDto;
import com.example.borys_mankowski_test_10.appuser.model.AppUserMapper;
import com.example.borys_mankowski_test_10.appuser.model.CreateAppUserCommand;
import com.example.borys_mankowski_test_10.email.EmailService;
import com.example.borys_mankowski_test_10.exception.DuplicateResourceException;
import com.example.borys_mankowski_test_10.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private AppUserService appUserService;

    @Mock
    private EmailService emailService;

    @Captor
    private ArgumentCaptor<AppUser> appUserArgumentCaptor;

    @Mock
    private AppUserMapper appUserMapper;

    @Test
    void testRegisterUser() throws Exception {

        CreateAppUserCommand command = CreateAppUserCommand.builder().firstName("John").lastName("Doe").email("john.doe@example.com").build();

        AppUser mappedAppUser = new AppUser();
        mappedAppUser.setFirstName("John");
        mappedAppUser.setLastName("Doe");
        mappedAppUser.setEmail("john.doe@example.com");
        mappedAppUser.setConfirmationToken("unique-token");
        mappedAppUser.setEnabled(false);

        AppUser savedAppUser = new AppUser();
        savedAppUser.setId(1L);
        savedAppUser.setFirstName("John");
        savedAppUser.setLastName("Doe");
        savedAppUser.setEmail("john.doe@example.com");
        savedAppUser.setEnabled(false);
        savedAppUser.setConfirmationToken("unique-token");

        AppUserDto expectedDto = new AppUserDto();
        expectedDto.setId(1L);
        expectedDto.setFirstName("John");
        expectedDto.setLastName("Doe");
        expectedDto.setEmail("john.doe@example.com");
        expectedDto.setEnabled(false);

        when(appUserMapper.fromDto(command)).thenReturn(mappedAppUser);
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser appUser = invocation.getArgument(0);
            appUser.setConfirmationToken("unique-token");
            return appUser;
        });
        when(appUserMapper.toDTO(any(AppUser.class))).thenReturn(expectedDto);


        AppUserDto result = appUserService.registerAppUser(command);


        assertEquals(expectedDto, result);
        verify(appUserRepository).save(appUserArgumentCaptor.capture());
        AppUser capturedAppUser = appUserArgumentCaptor.getValue();

        assertEquals(savedAppUser.getFirstName(), capturedAppUser.getFirstName());
        assertEquals(savedAppUser.getLastName(), capturedAppUser.getLastName());
        assertEquals(savedAppUser.getEmail(), capturedAppUser.getEmail());
        assertFalse(capturedAppUser.isEnabled());
        assertEquals(savedAppUser.getConfirmationToken(), capturedAppUser.getConfirmationToken());
        verify(emailService).sendConfirmationEmail(eq("john.doe@example.com"), eq("Email Confirmation"), anyString());

    }

    @Test
    public void testConfirmTokenWithEmptyToken() {
        assertThrows(ResourceNotFoundException.class, () -> appUserService.confirmToken(""));
    }

    @Test
    public void testConfirmTokenWithValidToken_UserAlreadyEnabled() {
        String token = "validToken";
        AppUser enabledAppUser = new AppUser();
        enabledAppUser.setEnabled(true);
        enabledAppUser.setConfirmationToken(token);

        when(appUserRepository.findAppUserByConfirmationToken(token)).thenReturn(Optional.of(enabledAppUser));

        assertThrows(DuplicateResourceException.class, () -> appUserService.confirmToken(token));
    }

    @Test()
    public void testConfirmTokenWithValidToken_Success() {
        String token = "validToken";
        AppUser disabledAppUser = new AppUser();
        disabledAppUser.setEnabled(false);

        when(appUserRepository.findAppUserByConfirmationToken(token)).thenReturn(Optional.of(disabledAppUser));

        assertDoesNotThrow(() -> appUserService.confirmToken(token));

        verify(appUserRepository, times(1)).findAppUserByConfirmationToken(token);
    }

    @Test
    public void testConfirmTokenWithValidToken_EnableAppUser() {
        String token = "validToken";
        AppUser workingAppUser = new AppUser();
        workingAppUser.setEmail("test@test.pl");

        AppUser sampleUser = new AppUser();
        sampleUser.setEmail("test@test.pl");

        when(appUserRepository.findAppUserByConfirmationToken(token)).thenReturn(Optional.of(sampleUser));

        assertTrue(verifyUsers(workingAppUser, appUserRepository.findAppUserByConfirmationToken(token).get()));
    }

    @Test
    public void testRegisterCustomerWithExistingEmail() {
        CreateAppUserCommand command = CreateAppUserCommand.builder().firstName("John").lastName("Doe").email("john@example.com").build();

        assertThrows(NullPointerException.class, () -> appUserService.registerAppUser(command));
    }

    @Test
    public void testConfirmEmailWithNullToken() {
        assertThrows(NullPointerException.class, () -> appUserService.confirmToken(null));
    }

    @Test
    public void testConfirmEmailWithEmptyToken() {
        assertThrows(ResourceNotFoundException.class, () -> appUserService.confirmToken(""));
    }

    private boolean verifyUsers(AppUser actual, AppUser expected) {
        assertEquals(actual.getEmail(), expected.getEmail());
        return true;
    }

    @Test
    public void testGetAllCustomers() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        when(appUserRepository.findAll(pageRequest)).thenReturn(Page.empty());
        Page<AppUserDto> result = appUserService.getAllUsers(pageRequest);
        assertEquals(Page.empty(), result);
    }
}
