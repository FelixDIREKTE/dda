package com.dd.dda.service;

import com.dd.dda.model.FileType;
import com.dd.dda.model.VerificationStatus;
import com.dd.dda.model.exception.DDAException;
import com.dd.dda.model.sqldata.User;
import com.dd.dda.repository.UserRepository;
import com.dd.dda.service.file.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private final SimpleDateFormat dateCsvFormat;

    private final FileStorageService fileStorageService;

    private final MailService mailService;

    private final ParliamentService parliamentService;



    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, FileStorageService fileStorageService, MailService mailService, ParliamentService parliamentService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.fileStorageService = fileStorageService;
        this.mailService = mailService;
        this.parliamentService = parliamentService;
        this.dateCsvFormat = new SimpleDateFormat("dd.MM.yyyy");
        dateCsvFormat.setTimeZone(TimeZone.getDefault());

    }

    public User authenticate(String emailLogin, String password) {

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.warn("Authentication sleep interrupted", e);
        }

        User dbUser = getUserByEmailAndPassword(emailLogin, password);
        haveFoundUser(dbUser);

        dbUser.setPassword(null);
        dbUser.setPasswordHash(null);

        return dbUser;
    }

    public User login(String error, Model model, Principal principal, RedirectAttributes flash) {
        try {
            Thread.sleep(1);
        } catch (Exception e) {
            log.warn("login sleep exception", e);
        }

        if (principal == null) {
            throw new DDAException("Internal server error, principal security is null");
        }

        if (StringUtils.isNotEmpty(error)) {
            log.warn("Some error happened.... => {}", error);
            throw new DDAException(error);
        }

        User user = getUserByMail(principal.getName());
        if (Boolean.FALSE.equals(user.isActive())) {
            model.addAttribute("error", "Benutzer ist nicht aktiv.");
            throw new DDAException("Der Benutzer ist nicht aktiv");
        }

        user.setPassword(null);
        user.setPasswordHash(null);
        flash.addFlashAttribute("info", "Sie sind schon angemeldet.");
        return user;
    }

    public User getUserByIdEncrypted(Long id) {
        if (id == null || id <= 0) {
            throw new DDAException("Negative Ids are not allowed");
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User userFound = user.get();
            userFound.setPassword(null);
            userFound.setPasswordHash(null);
            return userFound;
        }
        return null;
    }

    public User getUserByIdUnencrypted(Long id) {
        if (id == null || id <= 0) {
            throw new DDAException("Negative Ids are not allowed");
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User userFound = user.get();
            return userFound;
        }
        return null;
    }

    public void forgotPassword(String email){
        User u = getUserByMailWithoutPassword(email);


        String newPassword = RandomStringUtils.random(16, true, true);
        String pwEncoded = bCryptPasswordEncoder.encode(newPassword);

        u.setPasswordHash(pwEncoded);
        userRepository.save(u);
        mailService.sendPasswordReset(email, newPassword);

    }



    public User getUserByMail(String email) {
        if (email == null) {
            return null;
        }
        if (!email.matches(".+@.+\\..+")) {
            throw new DDAException("E-Mail-Adresse ist nicht gültig");
        }
        return userRepository.findByEmail(email);
    }

    public User getUserByMailWithoutPassword(String email) {
        User user = getUserByMail(email);

        if (user != null) {
            user.setPassword(null);
            user.setPasswordHash(null);
            return user;
        }
        return null;
    }

    public User getUserByEmailAndPassword(String emailLogin, String password) {
        if (emailLogin == null || emailLogin.isEmpty() || password == null || password.isEmpty()) {
            throw new DDAException("Email und Passwort müssen übergeben werden");
        }
        if (!emailLogin.matches(".+@.+\\..+")) {
            throw new DDAException("E-Mail-Adresse ist nicht gültig");
        }

        User dbUser = userRepository.findByEmailAndActiveTrue(emailLogin);
        haveFoundUser(dbUser);

        if (bCryptPasswordEncoder.matches(password, dbUser.getPasswordHash())) {
            dbUser.setPassword(null);
            dbUser.setPasswordHash(null);

            return dbUser;
        } else {
            return null;
        }
    }

    private void haveFoundUser(User user) {
        if (user == null || user.getId() == null) {
            throw new DDAException("Unbekannter Benutzer");
        }
    }

    @Transactional(rollbackFor = DDAException.class)
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        if (id == null || id < 0 || oldPassword == null || oldPassword.isEmpty() || newPassword == null || newPassword.isEmpty()) {
            log.error("Update Password faild no Id, oldPassword or new Password");
            return false;
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User dbUser = user.get();
            if (bCryptPasswordEncoder.matches(oldPassword, dbUser.getPasswordHash())) {
                dbUser.setPasswordHash(bCryptPasswordEncoder.encode(newPassword));

                return (updateUser(dbUser)) != null;
            } else {
                return false;
            }
        } else {
            log.error("Update Password faild no User find with this id:" + id);
            return false;
        }
    }

    @Transactional(rollbackFor = DDAException.class)
    public User updateUser(@Valid User user) {
        if (user == null) {
            throw new DDAException("No User was given to the function");
        }
        if (user.getId() == null) {
            throw new DDAException("Impossible to update a \"User\" with empty id");
        }
        if (user.isActive() == null || user.getEmail() == null) {
            throw new DDAException("Active and email must be be given");
        }

        final User save = userRepository.save(user);

        if (!save.getId().equals(user.getId())) {
            throw new DDAException("The given Id is wrong to perform an update, the operation is not permitted");
        }

        return save;
    }


    @Transactional(rollbackFor = Exception.class)
    public User createUser(@Valid User user) {
        if(getUserByMail(user.getEmail()) != null){
            throw new DDAException("Emailadresse in Benutzung");
        }
        return userRepository.save(user);
    }

    public String updateUserData(Long id, String name, String firstname, String street, String housenr, String zipcode, String birthdate) {

        if (id == null || id < 0 ) {
            log.error("Update Password faild no Id");
            return "Nutzer-Id fehlerhaft";
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User dbUser = user.get();

            if(name != null && !name.isEmpty() && !name.equals(dbUser.getName())){
                    dbUser.setVerificationstatus(VerificationStatus.DATANEEDED);
                    dbUser.setName(name);
            }
            if(firstname != null && !firstname.isEmpty() && !firstname.equals(dbUser.getFirstname())){
                dbUser.setVerificationstatus(VerificationStatus.DATANEEDED);
                dbUser.setFirstname(firstname);

            }
            if(zipcode != null && !zipcode.isEmpty() && !zipcode.equals(dbUser.getZipcode())){
                dbUser.setVerificationstatus(VerificationStatus.DATANEEDED);
                if(zipcode.length() != 5) {
                    return "PLZ muss aus 5 Ziffern bestehen";
                }
                try {
                    Integer.parseInt(zipcode);
                } catch (NumberFormatException nfe) {
                    return "PLZ muss aus 5 Ziffern bestehen";
                }

                dbUser.setZipcode(zipcode);
            }
            if(street != null && !street.isEmpty() && !street.equals(dbUser.getStreet())){
                dbUser.setVerificationstatus(VerificationStatus.DATANEEDED);
                dbUser.setStreet(street);
            }
            if(housenr != null && !housenr.isEmpty() && !housenr.equals(dbUser.getHousenr())){
                dbUser.setVerificationstatus(VerificationStatus.DATANEEDED);
                dbUser.setHousenr(housenr);
            }
            if(birthdate != null && !birthdate.isEmpty()){
                if(birthdate.length() != 10){
                    return "Falsches Format für Geburtsdatum";
                }
                Date bd = null;
                try {
                    bd = dateCsvFormat.parse(birthdate);
                } catch (ParseException e) {
                    return "Falsches Format für Geburtsdatum";
                }
                if(!bd.equals(dbUser.getBirthdate())) {
                    dbUser.setVerificationstatus(VerificationStatus.DATANEEDED);
                    dbUser.setBirthdate(bd);
                }
            }

            updateVerificationStatusAndSave(dbUser);
        } else {
            log.error("Update Userdata failed no User find with this id:" + id);
            return "Nutzer mit ID nicht vorhanden";
        }
        return "ok";


    }

    public void updateVerificationStatusAndSave(User user){
        boolean otherStuffThere =
                (          user.getVerificationstatus() == VerificationStatus.VERIFIED
                        || fileStorageService.filesExist(FileType.USERVERIFICATION, user.getId()))
                && parliamentService.hasAllParlimamentAccess(user.getId());
        user.updateVerificationStatus(otherStuffThere);
        userRepository.save(user);
    }

    public void updateVerificationStatusAndSave(Long id){
        User user = getUserByIdUnencrypted(id);
        if(user.getVerificationstatus() == VerificationStatus.VERIFIED){
            //Konsistenzcheck: alle Daten gegeben & keine VerificationFile
            if (fileStorageService.filesExist(FileType.USERVERIFICATION, id) ) {
                log.error("ERROR: User " + id + " ist verifiziert aber hat noch VerificationFiles");
            }
            if (!user.allDataPresent()) {
                log.error("ERROR: User " + id + " ist verifiziert aber hat nicht alle Daten");
            }

        }  else {

            updateVerificationStatusAndSave(user);
        }

    }

    public String changeEmail(Long id, String email) {


        if (id == null || id < 0 ) {
            log.error("Update Password faild no Id");
            return "Nutzer-Id fehlerhaft";
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User dbUser = user.get();

            if(email != null && !email.isEmpty()){
                dbUser.setEmail(email);
            }
            userRepository.save(dbUser);

        } else {
            log.error("Update Userdata failed no User find with this id:" + id);
            return "Nutzer mit ID nicht vorhanden";
        }
        return "ok";
    }

    public void deleteUser(Long id) {
        if (id == null || id < 0 ) {
            log.error("Delete USer faild no Id");
            return ;
        }
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            userRepository.delete(user.get());
        }
    }





    public String updateUserOptionalData(Long id, String phonenr) {


        if (id == null || id < 0 ) {
            log.error("Update Password faild no Id");
            return "Nutzer-Id fehlerhaft";
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User dbUser = user.get();

            if(phonenr != null && !phonenr.isEmpty()){
                dbUser.setPhonenr(phonenr);
            }
            userRepository.save(dbUser);
        } else {
            log.error("Update Userdata failed no User find with this id:" + id);
            return "Nutzer mit ID nicht vorhanden";
        }
        return "ok";

    }

    public void addCommentsRead(Long id, int length) {
        userRepository.addCommentsRead(id, length);
    }

    public void setToVerificationNeeded(Long id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User dbUser = user.get();
            dbUser.setVerificationstatus(VerificationStatus.DATANEEDED);
            updateVerificationStatusAndSave(dbUser);
        }
    }

    public int getCategories(Long id) {
        User u = getUserByIdUnencrypted(id);
        return u.getCategories_bitstring();
    }

    public void updateCategories(Long id, int categoryBits) {
        User u = getUserByIdUnencrypted(id);
        u.setCategories_bitstring(categoryBits);
        userRepository.save(u);
    }


    public List<User> getUsersCreated3DaysAgo() {
        Date d = new Date();
        Date d2 = DateUtils.addDays(d, -4);
        Date d3 = DateUtils.addDays(d, -3);
        List<User> result = userRepository.getUsersCreatedBetween(d2, d3);
        return result;
    }
}
