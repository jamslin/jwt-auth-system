import React, { useState, useRef } from 'react';
import { Container, Row, Col, Form, Button, Card, Alert, Badge, Tab, Tabs } from 'react-bootstrap';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';

// Define validation schemas
const loginSchema = z.object({
    username: z.string().min(1, 'Username is required'),
    password: z.string().min(6, 'Password must be at least 6 characters')
});

const registerSchema = z.object({
    username: z.string().min(1, 'Username is required'),
    email: z.string().email('Invalid email address'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    confirmPassword: z.string()
}).refine(data => data.password === data.confirmPassword, {
    message: "Passwords don't match",
    path: ["confirmPassword"]
});

type LoginFormData = z.infer<typeof loginSchema>;
type RegisterFormData = z.infer<typeof registerSchema>;

const ApiTester: React.FC = () => {
    // Authentication forms
    const {
        register: registerLogin,
        handleSubmit: handleLoginSubmit,
        formState: { errors: loginErrors }
    } = useForm<LoginFormData>({ resolver: zodResolver(loginSchema) });

    const {
        register: registerRegister,
        handleSubmit: handleRegisterSubmit,
        formState: { errors: registerErrors },
        watch: watchRegister
    } = useForm<RegisterFormData>({ resolver: zodResolver(registerSchema) });

    // API request state
    const [apiUrl, setApiUrl] = useState<string>('http://localhost:8080/api/test');
    const [apiMethod, setApiMethod] = useState<string>('GET');
    const [apiBody, setApiBody] = useState<string>('');
    const [useToken, setUseToken] = useState<boolean>(true);
    const [showSensitiveData, setShowSensitiveData] = useState<boolean>(false);

    // Response state
    const [response, setResponse] = useState<any>(null);
    const [responseStatus, setResponseStatus] = useState<number | null>(null);
    const [responseTime, setResponseTime] = useState<number | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);
    const [jwtToken, setJwtToken] = useState<string | null>(localStorage.getItem('jwt_token'));

    // Handle authentication
    const handleLogin = async (data: LoginFormData) => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch('http://localhost:8080/api/auth/login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            const result = await response.json();

            if (response.ok && result.token) {
                const token = result.token;
                localStorage.setItem('jwt_token', token);
                setJwtToken(token);
                alert('Login successful!');
            } else {
                setError(result.message || 'Login failed');
            }
        } catch (err) {
            setError('Network error occurred');
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async (data: RegisterFormData) => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch('http://localhost:8080/api/auth/register', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    username: data.username,
                    email: data.email,
                    password: data.password
                })
            });

            const result = await response.json();

            if (response.ok) {
                alert('Registration successful! Please log in.');
            } else {
                setError(result.message || 'Registration failed');
            }
        } catch (err) {
            setError('Network error occurred');
        } finally {
            setLoading(false);
        }
    };

    // Handle API request
    const handleApiRequest = async () => {
        if (!apiUrl) {
            setError('Please enter an API URL');
            return;
        }

        setLoading(true);
        setError(null);
        const startTime = Date.now();

        try {
            const headers: Record<string, string> = {
                'Content-Type': 'application/json'
            };

            if (useToken && jwtToken) {
                headers['Authorization'] = `Bearer ${jwtToken}`;
            }

            const options: RequestInit = {
                method: apiMethod,
                headers
            };

            if (apiMethod !== 'GET' && apiBody.trim()) {
                options.body = apiBody;
            }

            const response = await fetch(apiUrl, options);
            const result = await response.json();

            const endTime = Date.now();

            setResponse(result);
            setResponseStatus(response.status);
            setResponseTime(endTime - startTime);
        } catch (err) {
            setError('Network error occurred');
        } finally {
            setLoading(false);
        }
    };

    // Utility functions
    const copyToClipboard = (text: string) => {
        navigator.clipboard.writeText(text);
    };

    const clearResponse = () => {
        setResponse(null);
        setResponseStatus(null);
        setResponseTime(null);
        setError(null);
    };

    const clearToken = () => {
        localStorage.removeItem('jwt_token');
        setJwtToken(null);
        alert('Token cleared!');
    };

    // Truncate token for display
    const displayToken = jwtToken
        ? `${jwtToken.substring(0, 20)}...${jwtToken.substring(jwtToken.length - 10)}`
        : '';

    return (
        <Container fluid>
            <Row>
                <Col lg={6}>
                    <Card className="mb-4">
                        <Card.Header>
                            <h5>🔐 Authentication</h5>
                        </Card.Header>
                        <Card.Body>
                            <Tabs defaultActiveKey="login" id="auth-tabs" className="mb-3">
                                <Tab eventKey="login" title="Login">
                                    <Form onSubmit={handleLoginSubmit(handleLogin)}>
                                        <Form.Group className="mb-3">
                                            <Form.Label>Username</Form.Label>
                                            <Form.Control
                                                type="text"
                                                {...registerLogin('username')}
                                                placeholder="Enter username"
                                            />
                                            {loginErrors.username && (
                                                <Form.Text className="text-danger">
                                                    {loginErrors.username.message}
                                                </Form.Text>
                                            )}
                                        </Form.Group>

                                        <Form.Group className="mb-3">
                                            <Form.Label>Password</Form.Label>
                                            <Form.Control
                                                type="password"
                                                {...registerLogin('password')}
                                                placeholder="Enter password"
                                            />
                                            {loginErrors.password && (
                                                <Form.Text className="text-danger">
                                                    {loginErrors.password.message}
                                                </Form.Text>
                                            )}
                                        </Form.Group>

                                        <Button variant="primary" type="submit" disabled={loading}>
                                            {loading ? 'Signing In...' : 'Sign In'}
                                        </Button>
                                    </Form>
                                </Tab>

                                <Tab eventKey="register" title="Register">
                                    <Form onSubmit={handleRegisterSubmit(handleRegister)}>
                                        <Form.Group className="mb-3">
                                            <Form.Label>Username</Form.Label>
                                            <Form.Control
                                                type="text"
                                                {...registerRegister('username')}
                                                placeholder="Choose username"
                                            />
                                            {registerErrors.username && (
                                                <Form.Text className="text-danger">
                                                    {registerErrors.username.message}
                                                </Form.Text>
                                            )}
                                        </Form.Group>

                                        <Form.Group className="mb-3">
                                            <Form.Label>Email</Form.Label>
                                            <Form.Control
                                                type="email"
                                                {...registerRegister('email')}
                                                placeholder="Enter email"
                                            />
                                            {registerErrors.email && (
                                                <Form.Text className="text-danger">
                                                    {registerErrors.email.message}
                                                </Form.Text>
                                            )}
                                        </Form.Group>

                                        <Form.Group className="mb-3">
                                            <Form.Label>Password</Form.Label>
                                            <Form.Control
                                                type="password"
                                                {...registerRegister('password')}
                                                placeholder="Choose password"
                                            />
                                            {registerErrors.password && (
                                                <Form.Text className="text-danger">
                                                    {registerErrors.password.message}
                                                </Form.Text>
                                            )}
                                        </Form.Group>

                                        <Form.Group className="mb-3">
                                            <Form.Label>Confirm Password</Form.Label>
                                            <Form.Control
                                                type="password"
                                                {...registerRegister('confirmPassword')}
                                                placeholder="Confirm password"
                                            />
                                            {registerErrors.confirmPassword && (
                                                <Form.Text className="text-danger">
                                                    {registerErrors.confirmPassword.message}
                                                </Form.Text>
                                            )}
                                        </Form.Group>

                                        <Button variant="primary" type="submit" disabled={loading}>
                                            {loading ? 'Creating Account...' : 'Create Account'}
                                        </Button>
                                    </Form>
                                </Tab>
                            </Tabs>

                            {jwtToken && (
                                <div className="mt-3 p-3 bg-light rounded">
                                    <h6>🎫 JWT Token</h6>
                                    <div className="d-flex justify-content-between align-items-center">
                                        <code>
                                            {showSensitiveData ? jwtToken : displayToken}
                                        </code>
                                        <Button
                                            size="sm"
                                            variant={showSensitiveData ? "secondary" : "outline-secondary"}
                                            onClick={() => setShowSensitiveData(!showSensitiveData)}
                                        >
                                            {showSensitiveData ? 'Hide' : 'Show'} Token
                                        </Button>
                                    </div>
                                    <div className="mt-2">
                                        <Button size="sm" variant="danger" onClick={clearToken}>
                                            Clear Token
                                        </Button>
                                    </div>
                                </div>
                            )}
                        </Card.Body>
                    </Card>

                    <Card>
                        <Card.Header>
                            <h5>🚀 API Request Tester</h5>
                        </Card.Header>
                        <Card.Body>
                            <Form>
                                <Form.Group className="mb-3">
                                    <Form.Label>Endpoint URL</Form.Label>
                                    <Form.Control
                                        type="text"
                                        value={apiUrl}
                                        onChange={(e) => setApiUrl(e.target.value)}
                                        placeholder="http://localhost:8080/api/..."
                                    />
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label>Method</Form.Label>
                                    <Form.Select
                                        value={apiMethod}
                                        onChange={(e) => setApiMethod(e.target.value)}
                                    >
                                        <option value="GET">GET</option>
                                        <option value="POST">POST</option>
                                        <option value="PUT">PUT</option>
                                        <option value="DELETE">DELETE</option>
                                    </Form.Select>
                                </Form.Group>

                                <Form.Group className="mb-3">
                                    <Form.Label>Request Body (JSON)</Form.Label>
                                    <Form.Control
                                        as="textarea"
                                        rows={4}
                                        value={apiBody}
                                        onChange={(e) => setApiBody(e.target.value)}
                                        placeholder='{"key": "value"}'
                                    />
                                </Form.Group>

                                <Form.Check
                                    type="switch"
                                    id="use-token-switch"
                                    label="Include JWT Token in Authorization Header"
                                    checked={useToken}
                                    onChange={(e) => setUseToken(e.target.checked)}
                                />

                                <Button
                                    variant="primary"
                                    onClick={handleApiRequest}
                                    disabled={loading}
                                    className="mt-3"
                                >
                                    {loading ? 'Sending Request...' : 'Send Request'}
                                </Button>
                            </Form>
                        </Card.Body>
                    </Card>
                </Col>

                <Col lg={6}>
                    <Card>
                        <Card.Header>
                            <h5>📬 Response</h5>
                        </Card.Header>
                        <Card.Body>
                            {error && <Alert variant="danger">{error}</Alert>}

                            {responseStatus !== null && (
                                <div className="mb-3">
                                    <Badge
                                        bg={responseStatus >= 200 && responseStatus < 300 ? 'success' : 'danger'}
                                        className="me-2"
                                    >
                                        Status: {responseStatus}
                                    </Badge>
                                    {responseTime !== null && (
                                        <Badge bg="info">
                                            Time: {responseTime}ms
                                        </Badge>
                                    )}
                                </div>
                            )}

                            <Form.Check
                                type="switch"
                                id="show-sensitive-data"
                                label="Show Sensitive Data in Response"
                                checked={showSensitiveData}
                                onChange={(e) => setShowSensitiveData(e.target.checked)}
                                className="mb-3"
                            />

                            <div className="border rounded p-3" style={{ minHeight: '300px', backgroundColor: '#f8f9fa' }}>
                                <pre className="mb-0" style={{ fontSize: '0.9rem', maxHeight: '400px', overflow: 'auto' }}>
                                    {response
                                        ? showSensitiveData
                                            ? JSON.stringify(response, null, 2)
                                            : JSON.stringify(response, (key, value) =>
                                                key.toLowerCase().includes('token') || key.toLowerCase().includes('password')
                                                    ? '***HIDDEN***'
                                                    : value, 2)
                                        : 'Response will appear here...'}
                                </pre>
                            </div>

                            <div className="mt-3">
                                <Button
                                    variant="secondary"
                                    onClick={() => response && copyToClipboard(JSON.stringify(response, null, 2))}
                                    disabled={!response}
                                    className="me-2"
                                >
                                    📋 Copy Response
                                </Button>
                                <Button variant="outline-secondary" onClick={clearResponse}>
                                    🗑️ Clear
                                </Button>
                            </div>
                        </Card.Body>
                    </Card>
                </Col>
            </Row>
        </Container>
    );
};

export default ApiTester;