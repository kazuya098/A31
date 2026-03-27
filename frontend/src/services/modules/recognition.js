import service from '../api';

export function uploadImage(data) {
  return service({
    url: '/recognition/upload',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

export function getRecognitionRecords(params) {
  return service({
    url: '/recognition/records',
    method: 'get',
    params,
  });
}
