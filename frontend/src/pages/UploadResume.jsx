import { useState, useCallback } from 'react';

const UploadResume = () => {
    const [isDragging, setIsDragging] = useState(false);
    const [isUploading, setIsUploading] = useState(false);
    const [resumeData, setResumeData] = useState(null);
    const [error, setError] = useState(null);

    const handleDrag = useCallback((e) => {
        e.preventDefault();
        e.stopPropagation();
        if (e.type === "dragenter" || e.type === "dragover") {
            setIsDragging(true);
        } else if (e.type === "dragleave") {
            setIsDragging(false);
        }
    }, []);

    const uploadFile = async (file) => {
        if (!file || file.type !== "application/pdf") {
            setError("Please upload a valid PDF file.");
            return;
        }

        setIsUploading(true);
        setError(null);

        const formData = new FormData();
        formData.append("file", file);

        try {
            // Using the proxy configured in vite.config.js to hit the API Gateway
            const response = await fetch("/api/parser/upload", {
                method: "POST",
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('token')}`
                },
                body: formData,
            });

            if (response.status === 401 || response.status === 403) {
                // localStorage.removeItem('token');
                // window.location.href = '/login';
                const errText = await response.text();
                console.error("401/403 Error Body:", errText);
                throw new Error("Session expired. Please log in again. Backend returned: " + errText);
            }

            if (!response.ok) {
                throw new Error("Failed to parse resume. Server returned " + response.status);
            }

            const data = await response.json();
            setResumeData(data);
        } catch (err) {
            console.error(err);
            setError(err.message || "An error occurred while uploading.");
        } finally {
            setIsUploading(false);
            setIsDragging(false);
        }
    };

    const handleDrop = useCallback((e) => {
        e.preventDefault();
        e.stopPropagation();
        setIsDragging(false);
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            uploadFile(e.dataTransfer.files[0]);
        }
    }, []);

    const handleChange = (e) => {
        e.preventDefault();
        if (e.target.files && e.target.files[0]) {
            uploadFile(e.target.files[0]);
        }
    };

    const getInitials = (name) => {
        if (!name) return "JD";
        return name.split(" ").map(n => n[0]).join("").substring(0, 2).toUpperCase();
    };

    return (
        <>
            <div className="bg-glow"></div>
            <div className="container">

                <header>
                    <h1>Resume Intelligence</h1>
                    <p className="subtitle">Microservice AI-Powered Parser</p>
                </header>

                {/* Upload State */}
                {!resumeData && (
                    <div
                        className={`upload-container ${isDragging ? 'drag-active' : ''}`}
                        onDragEnter={handleDrag}
                        onDragLeave={handleDrag}
                        onDragOver={handleDrag}
                        onDrop={handleDrop}
                        onClick={() => document.getElementById('fileUpload').click()}
                    >
                        {isUploading ? (
                            <div className="loading-spinner"></div>
                        ) : (
                            <div className="upload-icon">📄</div>
                        )}

                        <h3 className="upload-text">
                            {isUploading ? "Extracting Entities..." : "Drag & Drop your Resume"}
                        </h3>
                        <p className="upload-subtext">
                            {isUploading ? "Our microservices are processing your PDF" : "Supports PDF files up to 5MB"}
                        </p>
                        {error && <p style={{ color: '#ef4444', marginTop: '1rem' }}>{error}</p>}

                        <input
                            type="file"
                            id="fileUpload"
                            className="file-input"
                            accept=".pdf"
                            onChange={handleChange}
                        />
                    </div>
                )}

                {/* Dashboard State */}
                {resumeData && (
                    <div className="dashboard">

                        {/* Left Sidebar */}
                        <div className="sidebar">
                            <div className="glass-card profile-card">
                                <div className="avatar">
                                    {getInitials(resumeData.name)}
                                </div>
                                <h2>{resumeData.name}</h2>

                                <div style={{ marginTop: '1.5rem' }}>
                                    {resumeData.email && <div className="contact-item">✉️ {resumeData.email}</div>}
                                    {resumeData.phnNo && <div className="contact-item">📱 {resumeData.phnNo}</div>}
                                </div>

                                <div className="skills-container">
                                    {resumeData.skills?.slice(0, 15).map((skill, index) => (
                                        <span key={index} className="skill-badge">{skill}</span>
                                    ))}
                                </div>
                            </div>

                            <div className="glass-card total-exp-card" style={{ marginTop: '2rem' }}>
                                <div className="exp-number">{resumeData.totalExperience || 0}</div>
                                <div style={{ color: 'var(--text-secondary)' }}>Years Experience</div>
                            </div>

                            <button
                                onClick={() => setResumeData(null)}
                                style={{
                                    width: '100%', padding: '1rem', marginTop: '1rem',
                                    background: 'transparent', border: '1px solid var(--accent-primary)',
                                    color: 'var(--accent-primary)', borderRadius: '12px', cursor: 'pointer',
                                    fontWeight: '600'
                                }}
                            >
                                Parse Another Resume
                            </button>
                            
                            <button
                                onClick={() => {
                                    localStorage.removeItem('token');
                                    window.location.href = '/login';
                                }}
                                style={{
                                    width: '100%', padding: '1rem', marginTop: '1rem',
                                    background: 'rgba(239, 68, 68, 0.1)', border: '1px solid rgba(239, 68, 68, 0.3)',
                                    color: '#ef4444', borderRadius: '12px', cursor: 'pointer',
                                    fontWeight: '600'
                                }}
                            >
                                Logout
                            </button>
                        </div>

                        {/* Right Main Content */}
                        <div className="main-content">

                            {/* Experience Section */}
                            {resumeData.experience && resumeData.experience.length > 0 && (
                                <div className="glass-card" style={{ marginBottom: '2rem' }}>
                                    <h3 className="section-title">Professional Experience</h3>
                                    <div className="timeline">
                                        {resumeData.experience.map((job, index) => (
                                            <div className="timeline-item" key={index}>
                                                <div className="timeline-header">
                                                    <div>
                                                        <div className="item-title">{job.role || "Professional"} {job.currentJob && <span className="badge">Current</span>}</div>
                                                        <div className="item-subtitle">{job.company}</div>
                                                    </div>
                                                    <div className="item-date">{job.duration}</div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Education Section */}
                            {resumeData.education && resumeData.education.length > 0 && (
                                <div className="glass-card">
                                    <h3 className="section-title">Education</h3>
                                    <div className="timeline">
                                        {resumeData.education.map((edu, index) => (
                                            <div className="timeline-item" key={index}>
                                                <div className="timeline-header">
                                                    <div>
                                                        <div className="item-title">{edu.institution}</div>
                                                        <div className="item-subtitle">{edu.degree} {edu.branch ? `in ${edu.branch}` : ''}</div>
                                                    </div>
                                                    <div className="item-date">{edu.passingYear}</div>
                                                </div>
                                                {edu.percentage && (
                                                    <div className="item-details">Score: {edu.percentage}</div>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}
                            {/* Projects Section */}
                            {resumeData.projects && resumeData.projects.length > 0 && (
                                <div className="glass-card" style={{ marginBottom: '2rem' }}>
                                    <h3 className="section-title">Projects</h3>
                                    <div className="timeline">
                                        {resumeData.projects.map((proj, index) => (
                                            <div className="timeline-item" key={index}>
                                                <div className="timeline-header">
                                                    <div>
                                                        <div className="item-title">{proj.title}</div>
                                                    </div>
                                                </div>
                                                {proj.technologies && (
                                                    <div style={{ marginTop: '0.5rem', marginBottom: '0.5rem', display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                                                        {typeof proj.technologies === 'string'
                                                            ? proj.technologies.split(',').map((tech, i) => (
                                                                <span key={i} className="badge" style={{ marginLeft: 0, fontSize: '0.75rem' }}>{tech.trim()}</span>
                                                            ))
                                                            : proj.technologies.map((tech, i) => (
                                                                <span key={i} className="badge" style={{ marginLeft: 0, fontSize: '0.75rem' }}>{tech.trim()}</span>
                                                            ))
                                                        }
                                                    </div>
                                                )}
                                                {proj.description && (
                                                    <div className="item-details" style={{ lineHeight: '1.6' }}>{proj.description}</div>
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Certifications Section */}
                            {resumeData.certifications && resumeData.certifications.length > 0 && (
                                <div className="glass-card">
                                    <h3 className="section-title">Certifications</h3>
                                    <ul style={{ listStyle: 'none', paddingLeft: '1rem', color: 'var(--text-secondary)', lineHeight: '2' }}>
                                        {resumeData.certifications.map((cert, index) => (
                                            <li key={index} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                                                <span style={{ color: 'var(--accent-secondary)' }}>▹</span> {cert}
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}

                        </div>
                    </div>
                )}

            </div>
        </>
    )

}   

export default UploadResume;