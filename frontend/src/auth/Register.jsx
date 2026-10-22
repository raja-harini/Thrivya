import { useState } from "react";
import {
  Link,
  useNavigate,
} from "react-router-dom";

import { registerUser } from "../api/authApi";
import "../styles/auth.css";

const ACCOUNT_ROLES = [
  {
    value: "STUDENT",
    label: "Student",
  },
  {
    value: "PLACEMENT_CELL",
    label: "College Placement Cell",
  },
  {
    value: "RECRUITER",
    label: "Recruiter",
  },
  {
    value: "TECH_INTERVIEWER",
    label: "Technical Interviewer",
  },
];

const INITIAL_FORM_DATA = {
  userName: "",
  email: "",
  password: "",
  accountRole: "STUDENT",
};

function extractRegistrationError(error) {
  const responseData = error.response?.data;

  /*
   * Your backend returns field validation errors here:
   *
   * {
   *   message: "Validation failed",
   *   errors: {
   *     accountRole: "Role is required"
   *   }
   * }
   */
  if (
    responseData?.errors &&
    typeof responseData.errors === "object" &&
    !Array.isArray(responseData.errors)
  ) {
    const fieldErrors = {};

    for (const [field, value] of Object.entries(
      responseData.errors
    )) {
      if (typeof value === "string") {
        fieldErrors[field] = value;
      } else if (Array.isArray(value)) {
        const messages = value.filter(
          (item) => typeof item === "string"
        );

        if (messages.length > 0) {
          fieldErrors[field] = messages.join(" ");
        }
      }
    }

    if (Object.keys(fieldErrors).length > 0) {
      return {
        message: Object.values(fieldErrors).join(" "),
        fieldErrors,
      };
    }
  }

  if (
    typeof responseData === "string" &&
    responseData.trim()
  ) {
    return {
      message: responseData,
      fieldErrors: {},
    };
  }

  if (
    typeof responseData?.message === "string" &&
    responseData.message.trim()
  ) {
    return {
      message: responseData.message,
      fieldErrors: {},
    };
  }

  if (!error.response && error.request) {
    return {
      message:
        "Unable to connect to the server. Check that the Spring Boot backend is running.",
      fieldErrors: {},
    };
  }

  return {
    message:
      "Registration failed. Please try again.",
    fieldErrors: {},
  };
}

export default function Register() {
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    ...INITIAL_FORM_DATA,
  });

  const [error, setError] = useState("");
  const [fieldErrors, setFieldErrors] = useState({});
  const [loading, setLoading] = useState(false);

  function handleChange(event) {
    const { name, value } = event.target;

    setFormData((previous) => ({
      ...previous,
      [name]: value,
    }));

    setFieldErrors((previous) => ({
      ...previous,
      [name]: "",
    }));

    setError("");
  }

  function validateForm() {
    const errors = {};

    if (!formData.userName.trim()) {
      errors.userName = "Full name is required.";
    }

    if (!formData.email.trim()) {
      errors.email = "Email is required.";
    }

    /*
     * Do not trim passwords:
     * spaces may intentionally be part of a password.
     */
    if (formData.password.length < 8) {
      errors.password =
        "Password must contain at least 8 characters.";
    }

    const validRole = ACCOUNT_ROLES.some(
      (role) =>
        role.value === formData.accountRole
    );

    if (!validRole) {
      errors.accountRole =
        "Please select a valid account type.";
    }

    return errors;
  }

  async function handleSubmit(event) {
    event.preventDefault();

    if (loading) {
      return;
    }

    setError("");
    setFieldErrors({});

    const validationErrors = validateForm();

    if (Object.keys(validationErrors).length > 0) {
      setFieldErrors(validationErrors);
      setError("Please correct the highlighted fields.");
      return;
    }

    setLoading(true);

    try {
      const payload = {
        userName: formData.userName.trim(),

        email: formData.email
          .trim()
          .toLowerCase(),

        password: formData.password,

        /*
         * Must match the current backend DTO field.
         */
        accountRole: formData.accountRole,
      };

      await registerUser(payload);

      /*
       * Registration does not automatically log the user in.
       * Do not store the returned registration JWT here.
       */
      navigate("/login", {
        replace: true,
        state: {
          message:
            "Registration successful. Please log in.",
        },
      });
    } catch (requestError) {
      /*
       * Do not log the complete Axios error object:
       * its request configuration can contain the password.
       */
      const registrationError =
        extractRegistrationError(requestError);

      setError(registrationError.message);
      setFieldErrors(
        registrationError.fieldErrors
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <p className="brand">Thrivya</p>

        <h1>Create your account</h1>

        <p className="auth-description">
          Register to begin your personalized career
          journey.
        </p>

        {error && (
          <div
            className="error-message"
            role="alert"
            aria-live="polite"
          >
            {error}
          </div>
        )}

        <form
          onSubmit={handleSubmit}
          aria-busy={loading}
        >
          <div className="form-group">
            <label htmlFor="userName">
              Full name
            </label>

            <input
              id="userName"
              name="userName"
              type="text"
              value={formData.userName}
              onChange={handleChange}
              placeholder="Enter your full name"
              autoComplete="name"
              disabled={loading}
              required
              aria-invalid={
                Boolean(fieldErrors.userName)
              }
              aria-describedby={
                fieldErrors.userName
                  ? "userName-error"
                  : undefined
              }
            />

            {fieldErrors.userName && (
              <p
                id="userName-error"
                className="field-error"
              >
                {fieldErrors.userName}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="email">
              Email
            </label>

            <input
              id="email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="Enter your email"
              autoComplete="email"
              disabled={loading}
              required
              aria-invalid={
                Boolean(fieldErrors.email)
              }
              aria-describedby={
                fieldErrors.email
                  ? "email-error"
                  : undefined
              }
            />

            {fieldErrors.email && (
              <p
                id="email-error"
                className="field-error"
              >
                {fieldErrors.email}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="password">
              Password
            </label>

            <input
              id="password"
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="Minimum 8 characters"
              autoComplete="new-password"
              minLength={8}
              disabled={loading}
              required
              aria-invalid={
                Boolean(fieldErrors.password)
              }
              aria-describedby={
                fieldErrors.password
                  ? "password-error"
                  : undefined
              }
            />

            {fieldErrors.password && (
              <p
                id="password-error"
                className="field-error"
              >
                {fieldErrors.password}
              </p>
            )}
          </div>

          <div className="form-group">
            <label htmlFor="accountRole">
              Account type
            </label>

            <select
              id="accountRole"
              name="accountRole"
              value={formData.accountRole}
              onChange={handleChange}
              disabled={loading}
              required
              aria-invalid={
                Boolean(fieldErrors.accountRole)
              }
              aria-describedby={
                fieldErrors.accountRole
                  ? "accountRole-error"
                  : undefined
              }
            >
              {ACCOUNT_ROLES.map((role) => (
                <option
                  key={role.value}
                  value={role.value}
                >
                  {role.label}
                </option>
              ))}
            </select>

            {fieldErrors.accountRole && (
              <p
                id="accountRole-error"
                className="field-error"
              >
                {fieldErrors.accountRole}
              </p>
            )}
          </div>

          <button
            type="submit"
            className="primary-button"
            disabled={loading}
          >
            {loading
              ? "Creating account..."
              : "Register"}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account?{" "}
          <Link
            to="/login"
            className="secondary-link"
          >
            Login here
          </Link>
        </p>
      </section>
    </main>
  );
}