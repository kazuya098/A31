import service from '../api';

export function login(data) {
  return service({
    url: '/auth/login',
    method: 'post',
    data,
  });
}

export function register(data) {
  return service({
    url: '/auth/register',
    method: 'post',
    data,
  });
}

export function logout() {
  return service({
    url: '/auth/logout',
    method: 'post',
  });
}

