import { useTranslation } from 'react-i18next';
import { Container, Row, Col } from 'react-bootstrap';

const About = () => {
  const { t } = useTranslation();

  return (
    <Container>
      <Row>
        <Col>
          <h1>{t('about')}</h1>
          <p>{t('about')}</p>
        </Col>
      </Row>
    </Container>
  );
};

export default About;