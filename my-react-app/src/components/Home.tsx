import { useTranslation } from 'react-i18next';
import { Container, Row, Col, Button } from 'react-bootstrap';

const Home = () => {
  const { t } = useTranslation();

  return (
    <Container>
      <Row>
        <Col>
          <h1>{t('welcome')}</h1>
          <p>{t('home')}</p>
        </Col>
      </Row>
    </Container>
  );
};

export default Home;