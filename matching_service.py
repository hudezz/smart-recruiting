from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from flask import Flask, request, jsonify

app = Flask(__name__)

def calculate_match_score(job_description, applicant_resume):
    tfidf_vectorizer = TfidfVectorizer(stop_words='english')
    tfidf_matrix = tfidf_vectorizer.fit_transform([job_description, applicant_resume])
    similarity = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:2])
    return round(similarity[0][0] * 100, 2)

@app.route('/match-score', methods=['POST'])
def match_score_endpoint():
    data = request.get_json()
    job_description = data.get('job_description')
    applicant_resume = data.get('applicant_resume')
    match_score = calculate_match_score(job_description, applicant_resume)
    return jsonify({'match_score': match_score})

if __name__ == '__main__':
    app.run(debug=True, port=5000)