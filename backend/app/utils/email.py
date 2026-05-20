import os
import smtplib
from email.mime.text import MIMEText
from dotenv import load_dotenv

load_dotenv()

EMAIL_USER = os.getenv("EMAIL_USER")
EMAIL_PASS = os.getenv("EMAIL_PASS")

def send_alert_email(destino: str, mensaje: str):
    if not destino or not EMAIL_USER:
        print("Email no configurado. No se envió alerta.")
        return
    msg = MIMEText(f"Alerta generada:\n\n{mensaje}")
    msg['Subject'] = f"[Indoora] Alerta: {mensaje[:50]}"
    msg['From'] = EMAIL_USER
    msg['To'] = destino
    try:
        server = smtplib.SMTP('smtp.gmail.com', 587)
        server.starttls()
        server.login(EMAIL_USER, EMAIL_PASS)
        server.send_message(msg)
        server.quit()
        print(f"Email enviado a {destino}")
    except Exception as e:
        print(f"Error enviando email: {e}")