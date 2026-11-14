"""TF-IDF matching microservice.

Scores how closely an applicant's resume matches a job description, as a
percentage from 0 to 100. The Spring Boot API calls this through
com.recruitment.api.client.PythonMlClient.

Run it with:

    pip install -r requirements.txt
    python matching_service.py

It listens on port 5000 by default; set PORT to change that, and set
FLASK_DEBUG=1 to turn on the reloader.
"""

import os

from flask import Flask, jsonify, request
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity

app = Flask(__name__)


def calculate_match_score(job_description, applicant_resume):
    """Return the cosine similarity of the two texts, as a 0-100 percentage."""
    tfidf_vectorizer = TfidfVectorizer(stop_words="english")
    tfidf_matrix = tfidf_vectorizer.fit_transform([job_description, applicant_resume])
    similarity = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:2])
    return round(float(similarity[0][0]) * 100, 2)


@app.route("/match-score", methods=["POST"])
def match_score_endpoint():
    data = request.get_json(silent=True) or {}
    job_description = data.get("job_description")
    applicant_resume = data.get("applicant_resume")

    # Without this, a missing or blank field reached the vectorizer and came
    # back as a 500 with a stack trace. The caller gets a clear 400 instead.
    missing = [
        name
        for name, value in (
            ("job_description", job_description),
            ("applicant_resume", applicant_resume),
        )
        if not isinstance(value, str) or not value.strip()
    ]
    if missing:
        return (
            jsonify({"error": "missing or blank field(s): " + ", ".join(missing)}),
            400,
        )

    try:
        match_score = calculate_match_score(job_description, applicant_resume)
    except ValueError as exc:
        # TfidfVectorizer raises this when both texts are nothing but stop
        # words, so there is no vocabulary left to compare.
        app.logger.warning("Could not score the pair: %s", exc)
        return jsonify({"error": "no comparable words in the supplied texts"}), 422

    return jsonify({"match_score": match_score})


@app.route("/health", methods=["GET"])
def health():
    """Liveness check, so the API can tell "down" from "scored zero"."""
    return jsonify({"status": "ok"})


if __name__ == "__main__":
    app.run(
        debug=os.environ.get("FLASK_DEBUG") == "1",
        port=int(os.environ.get("PORT", "5000")),
    )
