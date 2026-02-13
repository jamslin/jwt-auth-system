import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useTranslation } from 'react-i18next';
import { Container, Row, Col, Form, Button } from 'react-bootstrap';

// Define validation schema using Zod
const registerSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters long'),
  confirmPassword: z.string()
}).refine(data => data.password === data.confirmPassword, {
  message: "Passwords don't match",
  path: ["confirmPassword"]
});

type RegisterFormData = z.infer<typeof registerSchema>;

const Register = () => {
  const { t } = useTranslation();
  const {
    register,
    handleSubmit,
    formState: { errors }
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema)
  });

  const onSubmit = (data: RegisterFormData) => {
    console.log(data);
  };

  return (
    <Container>
      <Row className="justify-content-md-center">
        <Col md="6">
          <h2>{t('register')}</h2>
          <Form onSubmit={handleSubmit(onSubmit)}>
            <Form.Group className="mb-3" controlId="formEmail">
              <Form.Label>{t('email')}</Form.Label>
              <Form.Control
                type="email"
                {...register('email')}
                placeholder={t('email')}
              />
              {errors.email && (
                <Form.Text className="text-danger">
                  {errors.email.message}
                </Form.Text>
              )}
            </Form.Group>

            <Form.Group className="mb-3" controlId="formPassword">
              <Form.Label>{t('password')}</Form.Label>
              <Form.Control
                type="password"
                {...register('password')}
                placeholder={t('password')}
              />
              {errors.password && (
                <Form.Text className="text-danger">
                  {errors.password.message}
                </Form.Text>
              )}
            </Form.Group>

            <Form.Group className="mb-3" controlId="formConfirmPassword">
              <Form.Label>Confirm Password</Form.Label>
              <Form.Control
                type="password"
                {...register('confirmPassword')}
                placeholder="Confirm Password"
              />
              {errors.confirmPassword && (
                <Form.Text className="text-danger">
                  {errors.confirmPassword.message}
                </Form.Text>
              )}
            </Form.Group>

            <Button variant="primary" type="submit">
              {t('submit')}
            </Button>
          </Form>
        </Col>
      </Row>
    </Container>
  );
};

export default Register;