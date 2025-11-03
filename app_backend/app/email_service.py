# G:\SmartKisan_Project\app_backend\app\email_service.py
import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import os

# --- IMPORTANT: SET THESE ENVIRONMENT VARIABLES ---
# In your terminal:
# set EMAIL_HOST=smtp.gmail.com
# set EMAIL_PORT=587
# set EMAIL_USER=your-email@gmail.com
# set EMAIL_PASSWORD=your-google-app-password
EMAIL_HOST = os.environ.get("EMAIL_HOST", "smtp.gmail.com")
EMAIL_PORT = int(os.environ.get("EMAIL_PORT", 587))
EMAIL_USER = os.environ.get("EMAIL_USER")
EMAIL_PASSWORD = os.environ.get("EMAIL_PASSWORD")

# --- THIS IS THE FIX ---
# The parameter is now 'email', not 'email_to'
async def send_otp_email(email: str, otp: str):
# --- END FIX ---
    """Sends an email with the 6-digit OTP."""

    if not EMAIL_USER or not EMAIL_PASSWORD:
        print("="*50)
        print("ERROR: EMAIL_USER or EMAIL_PASSWORD not set.")
        print("Email service is disabled.")
        print("="*50)
        # In a real app, you wouldn't crash, but for testing, this is critical.
        # We'll let the error in main.py catch this.
        raise ValueError("Email service is not configured on the server.")

    message = MIMEMultipart("alternative")
    message["Subject"] = "Your Password Reset OTP"
    message["From"] = EMAIL_USER
    message["To"] = email

    # Create the plain-text and HTML version of your message
    text = f"""
    Hi,
    Your OTP for SmartKisaan is: {otp}
    It will expire in 10 minutes.
    """
    html = f"""
    <html>
    <body>
        <div style="font-family: Arial, sans-serif; line-height: 1.6;">
            <h2>Password Reset Request</h2>
            <p>Hi there,</p>
            <p>We received a request to reset the password for your SmartKisaan account.</p>
            <p>Your One-Time Password (OTP) is:</p>
            <h1 style="color: #4CAF50; letter-spacing: 2px; text-align: center;">{otp}</h1>
            <p>This OTP will expire in 10 minutes. If you did not make this request, please ignore this email.</p>
            <br>
            <p>Thanks,</p>
            <p>The SmartKisaan Team</p>
        </div>
    </body>
    </html>
    """

    # Turn these into plain/html MIMEText objects
    part1 = MIMEText(text, "plain")
    part2 = MIMEText(html, "html")

    # Add HTML/plain-text parts to MIMEMultipart message
    # The email client will try to render the last part first
    message.attach(part1)
    message.attach(part2)

    # Send the email
    # Note: This is synchronous. For a high-load app, you'd use a background task.
    try:
        with smtplib.SMTP(EMAIL_HOST, EMAIL_PORT) as server:
            server.starttls()  # Secure the connection
            server.login(EMAIL_USER, EMAIL_PASSWORD)
            server.sendmail(EMAIL_USER, email, message.as_string())
        print(f"Successfully sent OTP to {email}")
    except Exception as e:
        print(f"Error sending email to {email}: {e}")
        # Re-raise the exception so main.py can catch it
        raise e