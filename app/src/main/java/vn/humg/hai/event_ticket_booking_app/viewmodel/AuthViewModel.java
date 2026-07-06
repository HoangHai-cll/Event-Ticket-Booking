package vn.humg.hai.event_ticket_booking_app.viewmodel;

import vn.humg.hai.event_ticket_booking_app.controller.*;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import vn.humg.hai.event_ticket_booking_app.model.Admin;
import vn.humg.hai.event_ticket_booking_app.model.User;



public class AuthViewModel extends ViewModel {
    private final AuthController authRepository;
    private final UserController userRepository;

    private final MutableLiveData<Boolean> _loadingState = new MutableLiveData<>(false);
    private final MutableLiveData<String> _errorState = new MutableLiveData<>(null);
    private final MutableLiveData<FirebaseUser> _userSessionState = new MutableLiveData<>(null);
    private final MutableLiveData<User> _userProfileState = new MutableLiveData<>(null);
    private final MutableLiveData<Admin> _adminProfileState = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> _authSuccessState = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _updateSuccessState = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> _passwordChangeSuccessState = new MutableLiveData<>(false);

    public AuthViewModel() {
        this.authRepository = new AuthController();
        this.userRepository = new UserController();
        _userSessionState.setValue(authRepository.getAuth().getCurrentUser());
    }

    public LiveData<Boolean> getLoadingState() { return _loadingState; }
    public LiveData<String> getErrorState() { return _errorState; }
    public LiveData<FirebaseUser> getUserSessionState() { return _userSessionState; }
    public LiveData<User> getUserProfileState() { return _userProfileState; }
    public LiveData<Admin> getAdminProfileState() { return _adminProfileState; }
    public LiveData<Boolean> getAuthSuccessState() { return _authSuccessState; }
    public LiveData<Boolean> getUpdateSuccessState() { return _updateSuccessState; }
    public LiveData<Boolean> getPasswordChangeSuccessState() { return _passwordChangeSuccessState; }

    public void login(String email, String password) {
        _loadingState.setValue(true);
        _errorState.setValue(null);
        authRepository.getAuth().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = authRepository.getAuth().getCurrentUser();
                        _userSessionState.setValue(user);
                        if (user != null) {
                            userRepository.updateLastLogin(user.getUid(),
                                    () -> {
                                        _loadingState.postValue(false);
                                        _authSuccessState.postValue(true);
                                    },
                                    error -> {
                                        _loadingState.postValue(false);
                                        _authSuccessState.postValue(true); // Proceed anyway even if update lastLogin fails
                                    });
                        } else {
                            _loadingState.setValue(false);
                            _authSuccessState.setValue(true);
                        }
                    } else {
                        _loadingState.setValue(false);
                        _errorState.setValue(task.getException() != null ? task.getException().getMessage() : "Đăng nhập thất bại");
                    }
                });
    }

    public void register(String email, String password, String fullName, String phone, String adminCode) {
        _loadingState.setValue(true);
        _errorState.setValue(null);

        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("system_config").document("admin")
                .get()
                .addOnCompleteListener(configTask -> {
                    String dbSecretCode = "ADMIN123";
                    if (configTask.isSuccessful() && configTask.getResult() != null && configTask.getResult().exists()) {
                        String code = configTask.getResult().getString("secret_code");
                        if (code != null) {
                            dbSecretCode = code;
                        }
                    }
                    
                    final String finalSecretCode = dbSecretCode;

                    authRepository.getAuth().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    FirebaseUser firebaseUser = authRepository.getAuth().getCurrentUser();
                                    if (firebaseUser != null) {
                                        User userToSave;
                                        if (finalSecretCode.equals(adminCode)) {
                                            Admin admin = new Admin();
                                            admin.setUid(firebaseUser.getUid());
                                            admin.setFullName(fullName);
                                            admin.setEmail(email);
                                            admin.setPhone(phone);
                                            admin.setRole("admin");
                                            admin.setAccessLevel(1);
                                            admin.setCanManageEvents(true);
                                            admin.setCanManageBookings(true);
                                            admin.setAccountStatus("Active");
                                            admin.setPromotedAt(com.google.firebase.Timestamp.now());
                                            userToSave = admin;
                                        } else {
                                            userToSave = new User();
                                            userToSave.setUid(firebaseUser.getUid());
                                            userToSave.setFullName(fullName);
                                            userToSave.setEmail(email);
                                            userToSave.setPhone(phone);
                                            userToSave.setRole("user");
                                        }

                                        userRepository.saveUserProfile(userToSave,
                                                () -> {
                                                    _loadingState.postValue(false);
                                                    _authSuccessState.postValue(true);
                                                },
                                                errorMsg -> {
                                                    _loadingState.postValue(false);
                                                    _errorState.postValue(errorMsg);
                                                });
                                    } else {
                                        _loadingState.setValue(false);
                                        _authSuccessState.setValue(true);
                                    }
                                } else {
                                    _loadingState.setValue(false);
                                    _errorState.setValue(task.getException() != null ? task.getException().getMessage() : "Đăng ký thất bại");
                                }
                            });
                });
    }

    public void logout() {
        authRepository.getAuth().signOut();
        _userSessionState.setValue(null);
        _userProfileState.setValue(null);
        _adminProfileState.setValue(null);
        _authSuccessState.setValue(false);
    }

    public void getUserProfile(String uid) {
        _loadingState.setValue(true);
        userRepository.getUserById(uid,
                user -> {
                    _userProfileState.postValue(user);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }

    public void getAdminProfile(String uid) {
        _loadingState.setValue(true);
        userRepository.getAdminById(uid,
                admin -> {
                    _adminProfileState.postValue(admin);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }

    public void saveUserProfile(User user) {
        _loadingState.setValue(true);
        userRepository.saveUserProfile(user,
                () -> {
                    _userProfileState.postValue(user);
                    _updateSuccessState.postValue(true);
                    _loadingState.postValue(false);
                },
                error -> {
                    _errorState.postValue(error);
                    _loadingState.postValue(false);
                });
    }

    public void changePassword(String currentPass, String newPass) {
        FirebaseUser user = authRepository.getAuth().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            _errorState.setValue("Người dùng chưa đăng nhập");
            return;
        }

        _loadingState.setValue(true);
        _errorState.setValue(null);
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPass);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user.updatePassword(newPass).addOnCompleteListener(updateTask -> {
                    _loadingState.postValue(false);
                    if (updateTask.isSuccessful()) {
                        _passwordChangeSuccessState.postValue(true);
                    } else {
                        _errorState.postValue(updateTask.getException() != null ? updateTask.getException().getMessage() : "Lỗi đổi mật khẩu");
                    }
                });
            } else {
                _loadingState.postValue(false);
                _errorState.postValue("Mật khẩu hiện tại không đúng");
            }
        });
    }
}
