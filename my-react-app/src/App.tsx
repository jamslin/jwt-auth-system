import { Container, Nav, Navbar, NavDropdown } from 'react-bootstrap';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import Home from './components/Home';
import About from './components/About';
import Contact from './components/Contact';
import Login from './components/Login';
import Register from './components/Register';
import ApiTester from './components/ApiTester';
import './App.css';
import Tester from './components/Tester';

function App() {
  const { t, i18n } = useTranslation();

  const changeLanguage = (lng: string) => {
    i18n.changeLanguage(lng);
  };

  return (
    <Router>
      <div className="App">
        <Navbar bg="light" expand="lg">
          <Container>
            <Navbar.Brand as={Link} to="/">React App</Navbar.Brand>
            <Navbar.Toggle aria-controls="basic-navbar-nav" />
            <Navbar.Collapse id="basic-navbar-nav">
              <Nav className="me-auto">
                <Nav.Link as={Link} to="/">{t('home')}</Nav.Link>
                <Nav.Link as={Link} to="/about">{t('about')}</Nav.Link>
                <Nav.Link as={Link} to="/contact">{t('contact')}</Nav.Link>
                <Nav.Link as={Link} to="/login">{t('login')}</Nav.Link>
                <Nav.Link as={Link} to="/register">{t('register')}</Nav.Link>
                <Nav.Link as={Link} to="/api-tester">API Test</Nav.Link>
              </Nav>
              <Nav>
                <NavDropdown title={i18n.language} id="language-dropdown">
                  <NavDropdown.Item onClick={() => changeLanguage('en')}>English</NavDropdown.Item>
                  <NavDropdown.Item onClick={() => changeLanguage('tc')}>繁體中文</NavDropdown.Item>
                  <NavDropdown.Item onClick={() => changeLanguage('sc')}>简体中文</NavDropdown.Item>
                </NavDropdown>
              </Nav>
            </Navbar.Collapse>
          </Container>
        </Navbar>

        <Container className="mt-3">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/about" element={<About />} />
            <Route path="/contact" element={<Contact />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/api-tester" element={<ApiTester />} />
          </Routes>
        </Container>
      </div>
      <Tester />
    </Router>
  );
}

export default App;