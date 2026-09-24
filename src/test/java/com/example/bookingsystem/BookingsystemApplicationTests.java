package com.example.bookingsystem;

import com.example.bookingsystem.entity.AccessLevel;
import com.example.bookingsystem.entity.Account;
import com.example.bookingsystem.entity.Asset;
import com.example.bookingsystem.repository.AccountRepository;
import com.example.bookingsystem.repository.AssetRepository;
import com.example.bookingsystem.repository.BookingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingsystemApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private BookingRepository bookingRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private Long testAssetId;

	@BeforeEach
	void setUp() {

		bookingRepository.deleteAll();
		accountRepository.deleteAll();
		assetRepository.deleteAll();

		// -----------------------------------------------------
		// ADMIN
		// -----------------------------------------------------

		Account admin = new Account();

		admin.setUsername("test-admin");
		admin.setEmail("test-admin@example.com");
		admin.setPasswordHash(
				passwordEncoder.encode("Admin@123"));
		admin.setAccessLevel(AccessLevel.ADMIN);
		admin.setEnabled(true);

		accountRepository.save(admin);

		// -----------------------------------------------------
		// USER 1
		// -----------------------------------------------------

		Account user = new Account();

		user.setUsername("test-user");
		user.setEmail("test-user@example.com");
		user.setPasswordHash(
				passwordEncoder.encode("User@123"));
		user.setAccessLevel(AccessLevel.USER);
		user.setEnabled(true);

		accountRepository.save(user);

		// -----------------------------------------------------
		// USER 2
		// Used for ownership/security tests
		// -----------------------------------------------------

		Account user2 = new Account();

		user2.setUsername("test-user-2");
		user2.setEmail("test-user-2@example.com");
		user2.setPasswordHash(
				passwordEncoder.encode("User2@123"));
		user2.setAccessLevel(AccessLevel.USER);
		user2.setEnabled(true);

		accountRepository.save(user2);

		// -----------------------------------------------------
		// TEST ASSET
		// -----------------------------------------------------

		Asset asset = new Asset();

		asset.setName("Test Meeting Room");
		asset.setCategory("ROOM");
		asset.setDescription(
				"Room used for integration testing");
		asset.setPrice(new BigDecimal("500.00"));
		asset.setAvailable(true);

		Asset savedAsset =
				assetRepository.save(asset);

		testAssetId = savedAsset.getId();
	}

	// =========================================================
	// APPLICATION CONTEXT
	// =========================================================

	@Test
	void contextLoads() {
	}

	// =========================================================
	// AUTHENTICATION
	// =========================================================

	@Test
	void loginWithValidCredentialsReturnsJwt()
			throws Exception {

		mockMvc.perform(
						post("/auth/login")
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "username": "test-user",
                                            "password": "User@123"
                                        }
                                        """))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.username")
						.value("test-user"))
				.andExpect(jsonPath("$.role")
						.value("USER"));
	}

	@Test
	void adminLoginReturnsAdminRole()
			throws Exception {

		mockMvc.perform(
						post("/auth/login")
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "username": "test-admin",
                                            "password": "Admin@123"
                                        }
                                        """))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").exists())
				.andExpect(jsonPath("$.username")
						.value("test-admin"))
				.andExpect(jsonPath("$.role")
						.value("ADMIN"));
	}

	@Test
	void loginWithInvalidCredentialsReturnsUnauthorized()
			throws Exception {

		mockMvc.perform(
						post("/auth/login")
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "username": "test-user",
                                            "password": "WrongPassword"
                                        }
                                        """))
				.andExpect(status().isUnauthorized());
	}

	// =========================================================
	// AUTHORIZATION
	// =========================================================

	@Test
	void unauthenticatedRequestIsRejected()
			throws Exception {

		mockMvc.perform(
						get("/api/assets"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void tamperedJwtIsRejected()
			throws Exception {

		String token =
				login("test-user", "User@123");

		String tamperedToken =
				token.substring(0, token.length() - 1)
						+ (token.endsWith("a") ? "b" : "a");

		mockMvc.perform(
						get("/api/assets")
								.header(
										"Authorization",
										"Bearer " + tamperedToken))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void userCanViewAssets()
			throws Exception {

		String token =
				login("test-user", "User@123");

		mockMvc.perform(
						get("/api/assets")
								.header(
										"Authorization",
										"Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void userCannotCreateAsset()
			throws Exception {

		String token =
				login("test-user", "User@123");

		mockMvc.perform(
						post("/api/assets")
								.header(
										"Authorization",
										"Bearer " + token)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "name": "Restricted Room",
                                            "category": "ROOM",
                                            "description": "Should not be created by USER",
                                            "price": 1000.00,
                                            "available": true
                                        }
                                        """))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminCanCreateAsset()
			throws Exception {

		String token =
				login("test-admin", "Admin@123");

		mockMvc.perform(
						post("/api/assets")
								.header(
										"Authorization",
										"Bearer " + token)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "name": "Admin Room",
                                            "category": "ROOM",
                                            "description": "Created by admin",
                                            "price": 1200.00,
                                            "available": true
                                        }
                                        """))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name")
						.value("Admin Room"));
	}

	// =========================================================
	// USER BOOKING CREATION
	// =========================================================

	@Test
	void userCanCreateBooking()
			throws Exception {

		String token =
				login("test-user", "User@123");

		mockMvc.perform(
						post("/api/bookings")
								.header(
										"Authorization",
										"Bearer " + token)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "assetId": %d,
                                            "startAt": "2027-01-10T10:00:00",
                                            "endAt": "2027-01-10T12:00:00"
                                        }
                                        """.formatted(testAssetId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username")
						.value("test-user"))
				.andExpect(jsonPath("$.state")
						.value("PENDING"));
	}

	@Test
	void userCanViewOwnBookings()
			throws Exception {

		String token =
				login("test-user", "User@123");

		mockMvc.perform(
						post("/api/bookings")
								.header(
										"Authorization",
										"Bearer " + token)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "assetId": %d,
                                            "startAt": "2027-02-10T10:00:00",
                                            "endAt": "2027-02-10T12:00:00"
                                        }
                                        """.formatted(testAssetId)))
				.andExpect(status().isCreated());

		mockMvc.perform(
						get("/api/bookings/my")
								.header(
										"Authorization",
										"Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content")
						.isArray())
				.andExpect(jsonPath("$.content[0].username")
						.value("test-user"));
	}

	// =========================================================
	// BOOKING OWNERSHIP
	// =========================================================

	@Test
	void userCannotViewAnotherUsersBooking()
			throws Exception {

		String user1Token =
				login("test-user", "User@123");

		String user2Token =
				login("test-user-2", "User2@123");

		String response =
				mockMvc.perform(
								post("/api/bookings")
										.header(
												"Authorization",
												"Bearer " + user1Token)
										.contentType(
												MediaType.APPLICATION_JSON)
										.content("""
                                                {
                                                    "assetId": %d,
                                                    "startAt": "2027-05-10T10:00:00",
                                                    "endAt": "2027-05-10T12:00:00"
                                                }
                                                """.formatted(testAssetId)))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getContentAsString();

		long bookingId =
				extractLong(response, "id");

		mockMvc.perform(
						get("/api/bookings/" + bookingId)
								.header(
										"Authorization",
										"Bearer " + user2Token))
				.andExpect(status().isNotFound());
	}

	// =========================================================
	// USER ACCESS TO ADMIN BOOKINGS
	// =========================================================

	@Test
	void userCannotAccessAdminBookings()
			throws Exception {

		String token =
				login("test-user", "User@123");

		mockMvc.perform(
						get("/api/admin/bookings")
								.header(
										"Authorization",
										"Bearer " + token))
				.andExpect(status().isForbidden());
	}

	// =========================================================
	// ADMIN BOOKING CRUD
	// =========================================================

	@Test
	void adminCanCreateBooking()
			throws Exception {

		String token =
				login("test-admin", "Admin@123");

		long userId =
				accountRepository
						.findByUsername("test-user")
						.orElseThrow()
						.getId();

		mockMvc.perform(
						post("/api/admin/bookings")
								.header(
										"Authorization",
										"Bearer " + token)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "accountId": %d,
                                            "assetId": %d,
                                            "startAt": "2027-06-10T10:00:00",
                                            "endAt": "2027-06-10T12:00:00",
                                            "state": "CONFIRMED"
                                        }
                                        """.formatted(
										userId,
										testAssetId)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username")
						.value("test-user"))
				.andExpect(jsonPath("$.state")
						.value("CONFIRMED"));
	}

	@Test
	void adminCanUpdateBooking()
			throws Exception {

		String adminToken =
				login("test-admin", "Admin@123");

		String userToken =
				login("test-user", "User@123");

		String response =
				mockMvc.perform(
								post("/api/bookings")
										.header(
												"Authorization",
												"Bearer " + userToken)
										.contentType(
												MediaType.APPLICATION_JSON)
										.content("""
                                                {
                                                    "assetId": %d,
                                                    "startAt": "2027-07-10T10:00:00",
                                                    "endAt": "2027-07-10T12:00:00"
                                                }
                                                """.formatted(testAssetId)))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getContentAsString();

		long bookingId =
				extractLong(response, "id");

		long userId =
				accountRepository
						.findByUsername("test-user")
						.orElseThrow()
						.getId();

		mockMvc.perform(
						put("/api/admin/bookings/" + bookingId)
								.header(
										"Authorization",
										"Bearer " + adminToken)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "accountId": %d,
                                            "assetId": %d,
                                            "startAt": "2027-07-10T13:00:00",
                                            "endAt": "2027-07-10T15:00:00",
                                            "state": "CONFIRMED"
                                        }
                                        """.formatted(
										userId,
										testAssetId)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.state")
						.value("CONFIRMED"));
	}

	@Test
	void adminCanChangeBookingState()
			throws Exception {

		String adminToken =
				login("test-admin", "Admin@123");

		String userToken =
				login("test-user", "User@123");

		String response =
				mockMvc.perform(
								post("/api/bookings")
										.header(
												"Authorization",
												"Bearer " + userToken)
										.contentType(
												MediaType.APPLICATION_JSON)
										.content("""
                                                {
                                                    "assetId": %d,
                                                    "startAt": "2027-08-10T10:00:00",
                                                    "endAt": "2027-08-10T12:00:00"
                                                }
                                                """.formatted(testAssetId)))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getContentAsString();

		long bookingId =
				extractLong(response, "id");

		mockMvc.perform(
						patch("/api/admin/bookings/"
								+ bookingId
								+ "/state")
								.header(
										"Authorization",
										"Bearer " + adminToken)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "state": "CONFIRMED"
                                        }
                                        """))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.state")
						.value("CONFIRMED"));
	}

	@Test
	void cancelledBookingCannotBeConfirmedAgain()
			throws Exception {

		String adminToken =
				login("test-admin", "Admin@123");

		String userToken =
				login("test-user", "User@123");

		String response =
				mockMvc.perform(
								post("/api/bookings")
										.header(
												"Authorization",
												"Bearer " + userToken)
										.contentType(
												MediaType.APPLICATION_JSON)
										.content("""
                                                {
                                                    "assetId": %d,
                                                    "startAt": "2027-11-10T10:00:00",
                                                    "endAt": "2027-11-10T12:00:00"
                                                }
                                                """.formatted(testAssetId)))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getContentAsString();

		long bookingId =
				extractLong(response, "id");

		mockMvc.perform(
						patch("/api/admin/bookings/"
								+ bookingId
								+ "/state")
								.header(
										"Authorization",
										"Bearer " + adminToken)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "state": "CANCELLED"
                                        }
                                        """))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.state")
						.value("CANCELLED"));

		mockMvc.perform(
						patch("/api/admin/bookings/"
								+ bookingId
								+ "/state")
								.header(
										"Authorization",
										"Bearer " + adminToken)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "state": "CONFIRMED"
                                        }
                                        """))
				.andExpect(status().isBadRequest());
	}

	@Test
	void cancelledBookingDoesNotBlockNewBooking()
			throws Exception {

		String userToken =
				login("test-user", "User@123");

		String adminToken =
				login("test-admin", "Admin@123");

		String bookingJson = """
                {
                    "assetId": %d,
                    "startAt": "2027-12-10T10:00:00",
                    "endAt": "2027-12-10T12:00:00"
                }
                """.formatted(testAssetId);

		String response =
				mockMvc.perform(
								post("/api/bookings")
										.header(
												"Authorization",
												"Bearer " + userToken)
										.contentType(
												MediaType.APPLICATION_JSON)
										.content(bookingJson))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getContentAsString();

		long bookingId =
				extractLong(response, "id");

		mockMvc.perform(
						patch("/api/admin/bookings/"
								+ bookingId
								+ "/state")
								.header(
										"Authorization",
										"Bearer " + adminToken)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "state": "CANCELLED"
                                        }
                                        """))
				.andExpect(status().isOk());

		mockMvc.perform(
						post("/api/bookings")
								.header(
										"Authorization",
										"Bearer " + userToken)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content(bookingJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.state")
						.value("PENDING"));
	}

	@Test
	void adminCanDeleteBooking()
			throws Exception {

		String adminToken =
				login("test-admin", "Admin@123");

		String userToken =
				login("test-user", "User@123");

		String response =
				mockMvc.perform(
								post("/api/bookings")
										.header(
												"Authorization",
												"Bearer " + userToken)
										.contentType(
												MediaType.APPLICATION_JSON)
										.content("""
                                                {
                                                    "assetId": %d,
                                                    "startAt": "2027-09-10T10:00:00",
                                                    "endAt": "2027-09-10T12:00:00"
                                                }
                                                """.formatted(testAssetId)))
						.andExpect(status().isCreated())
						.andReturn()
						.getResponse()
						.getContentAsString();

		long bookingId =
				extractLong(response, "id");

		mockMvc.perform(
						delete("/api/admin/bookings/" + bookingId)
								.header(
										"Authorization",
										"Bearer " + adminToken))
				.andExpect(status().isNoContent());

		mockMvc.perform(
						get("/api/admin/bookings/" + bookingId)
								.header(
										"Authorization",
										"Bearer " + adminToken))
				.andExpect(status().isNotFound());
	}

	// =========================================================
	// ADMIN BOOKING LIST
	// =========================================================

	@Test
	void adminCanViewAllBookings()
			throws Exception {

		String token =
				login("test-admin", "Admin@123");

		mockMvc.perform(
						get("/api/admin/bookings")
								.header(
										"Authorization",
										"Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content")
						.isArray());
	}

	// =========================================================
	// VALIDATION
	// =========================================================

	@Test
	void invalidBookingTimeIsRejected()
			throws Exception {

		String token =
				login("test-user", "User@123");

		mockMvc.perform(
						post("/api/bookings")
								.header(
										"Authorization",
										"Bearer " + token)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "assetId": %d,
                                            "startAt": "2027-03-10T12:00:00",
                                            "endAt": "2027-03-10T10:00:00"
                                        }
                                        """.formatted(testAssetId)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void invalidAssetIdIsRejected()
			throws Exception {

		String token =
				login("test-user", "User@123");

		mockMvc.perform(
						post("/api/bookings")
								.header(
										"Authorization",
										"Bearer " + token)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content("""
                                        {
                                            "assetId": 999999,
                                            "startAt": "2027-10-10T10:00:00",
                                            "endAt": "2027-10-10T12:00:00"
                                        }
                                        """))
				.andExpect(status().isNotFound());
	}

	@Test
	void invalidPriceRangeIsRejected()
			throws Exception {

		String token =
				login("test-user", "User@123");

		mockMvc.perform(
						get("/api/assets")
								.param("minPrice", "1000")
								.param("maxPrice", "500")
								.header(
										"Authorization",
										"Bearer " + token))
				.andExpect(status().isBadRequest());
	}

	// =========================================================
	// BOOKING CONFLICT
	// =========================================================

	@Test
	void bookingConflictIsRejected()
			throws Exception {

		String token =
				login("test-user", "User@123");

		String booking = """
                {
                    "assetId": %d,
                    "startAt": "2027-04-10T10:00:00",
                    "endAt": "2027-04-10T12:00:00"
                }
                """.formatted(testAssetId);

		mockMvc.perform(
						post("/api/bookings")
								.header(
										"Authorization",
										"Bearer " + token)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content(booking))
				.andExpect(status().isCreated());

		mockMvc.perform(
						post("/api/bookings")
								.header(
										"Authorization",
										"Bearer " + token)
								.contentType(
										MediaType.APPLICATION_JSON)
								.content(booking))
				.andExpect(status().isConflict());
	}

	// =========================================================
	// FILTERING / PAGINATION / SORTING
	// =========================================================

	@Test
	void assetFilteringAndPaginationWorks()
			throws Exception {

		String token =
				login("test-user", "User@123");

		mockMvc.perform(
						get("/api/assets")
								.param("minPrice", "100")
								.param("maxPrice", "1000")
								.param("page", "0")
								.param("size", "10")
								.param("sortBy", "price")
								.param("direction", "asc")
								.header(
										"Authorization",
										"Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content")
						.isArray())
				.andExpect(jsonPath("$.number")
						.value(0))
				.andExpect(jsonPath("$.size")
						.value(10));
	}

	@Test
	void adminBookingFilteringAndSortingWorks()
			throws Exception {

		String token =
				login("test-admin", "Admin@123");

		mockMvc.perform(
						get("/api/admin/bookings")
								.param("page", "0")
								.param("size", "10")
								.param("sortBy", "price")
								.param("direction", "asc")
								.header(
										"Authorization",
										"Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content")
						.isArray());
	}

	@Test
	void invalidPaginationIsRejected()
			throws Exception {

		String token =
				login("test-user", "User@123");

		mockMvc.perform(
						get("/api/assets")
								.param("page", "-1")
								.param("size", "10")
								.header(
										"Authorization",
										"Bearer " + token))
				.andExpect(status().isBadRequest());
	}

	// =========================================================
	// HELPERS
	// =========================================================

	private String login(
			String username,
			String password) throws Exception {

		String response =
				mockMvc.perform(
								post("/auth/login")
										.contentType(
												MediaType.APPLICATION_JSON)
										.content("""
                                                {
                                                    "username": "%s",
                                                    "password": "%s"
                                                }
                                                """.formatted(
												username,
												password)))
						.andExpect(status().isOk())
						.andReturn()
						.getResponse()
						.getContentAsString();

		JsonNode json =
				objectMapper.readTree(response);

		JsonNode tokenNode =
				json.get("token");

		if (tokenNode == null
				|| tokenNode.isNull()
				|| tokenNode.asText().isBlank()) {

			throw new IllegalStateException(
					"Login response did not contain a valid JWT token");
		}

		return tokenNode.asText();
	}

	private long extractLong(
			String response,
			String field) {

		try {

			JsonNode json =
					objectMapper.readTree(response);

			JsonNode value =
					json.get(field);

			if (value == null
					|| value.isNull()
					|| !value.isNumber()) {

				throw new IllegalArgumentException(
						"Response does not contain numeric field: "
								+ field);
			}

			return value.longValue();

		} catch (Exception exception) {

			throw new IllegalArgumentException(
					"Unable to extract numeric field '"
							+ field
							+ "' from response",
					exception);
		}
	}
}