import requests

# This is the URL where your Flask app is listening
url = 'http://127.0.0.1:5000/predict'

# This is our fake student data.
# They tried skill 5 (got it right), skill 12 (got it wrong), and skill 8 (got it right)
fake_student_data = {
    "skills": [5, 12, 8],
    "corrects": [1, 0, 1]
}

print("Sending data to the AI...")

# Send the data to your API
response = requests.post(url, json=fake_student_data)

# Print out whatever the AI sends back!
print("\n--- AI Response ---")
print(response.json())