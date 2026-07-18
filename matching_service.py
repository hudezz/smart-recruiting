from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity


def calculate_match_score(job_description, applicant_resume):
    tfidf_vectorizer = TfidfVectorizer(stop_words='english')
    tfidf_matrix = tfidf_vectorizer.fit_transform([job_description, applicant_resume])
    similarity = cosine_similarity(tfidf_matrix[0:1], tfidf_matrix[1:2])
    similarity_score = similarity[0][0] * 100
    rounded_score = round(similarity_score, 2)
    return rounded_score

job_description = "We are looking for a software engineer with experience in Python, machine learning, and data analysis."
applicant_resume = "Passionate pastry chef with ten years of experience in French baking, pastry design, and kitchen management."
match_score = calculate_match_score(job_description, applicant_resume)
print(f"Match Score: {match_score}%")
