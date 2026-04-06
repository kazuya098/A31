import service from '../api';

export function getUserProfile() {
  return service({
    url: '/user/profile',
    method: 'get',
  });
}

export function updatePassword(data) {
  return service({
    url: '/user/password',
    method: 'put',
    data,
  });
}

export function restoreData() {
  return service({
    url: '/user/restore-data',
    method: 'post',
  });
}

