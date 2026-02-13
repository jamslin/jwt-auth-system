import { useTranslation } from 'react-i18next';
import { Container, Row, Col } from 'react-bootstrap';

const Contact = () => {
  const { t } = useTranslation();

  return (
    <Container>
      <Row>
        <Col>
          <h1>{t('contact')}</h1>
          <p>{t('contact')}</p>
        </Col>
      </Row>
    </Container>
  );
};

export default Contact;