package com.kindroid.hub.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.conn.ConnectTimeoutException;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.kindroid.hub.entity.User;
import com.kindroid.hub.entity.WeiboInfo;
import com.kindroid.hub.proto.DistributedClientProtoc.Client;
import com.kindroid.hub.proto.DistributedClientProtoc.ClientRequest;
import com.kindroid.hub.proto.DistributedClientProtoc.ClientResponse;
import com.kindroid.hub.proto.ResponseProtoc.Response.ResultType;
import com.kindroid.hub.proto.UserProtoc.Account;
import com.kindroid.hub.proto.UserProtoc.EditIconRequest;
import com.kindroid.hub.proto.UserProtoc.EditNickNameRequest;
import com.kindroid.hub.proto.UserProtoc.EditPassWordRequest;
import com.kindroid.hub.proto.UserProtoc.EditResponse;
import com.kindroid.hub.proto.UserProtoc.GeneralSignInRequest;
import com.kindroid.hub.proto.UserProtoc.SignInResponse;
import com.kindroid.hub.proto.UserProtoc.SignUpRequest;
import com.kindroid.hub.proto.UserProtoc.SignUpResponse;
import com.kindroid.hub.proto.UserProtoc.UserForgotPasswordRequest;
import com.kindroid.hub.proto.UserProtoc.UserForgotPasswordResponse;
import com.kindroid.hub.proto.UserProtoc.WeiboSignInRequest;
import com.kindroid.hub.proto.UserProtoc.WeiboSignInRequest.Type;
import com.kindroid.hub.utils.Constant;
import com.kindroid.hub.utils.ConvertUtils;
import com.kindroid.hub.utils.HttpRequest;

public class UserData {
	
	public Context context;

	public UserData(Context ctx) {
		this.context = ctx;
	}

	
	/**
	 * 
	 * @param 
	 * @return -1 name or password wrong
	 */
	public int userLogin(User user) {
		GeneralSignInRequest.Builder request = GeneralSignInRequest.newBuilder();

		Account.Builder account = Account.newBuilder();
		account.setEmail(user.getUsername());
		account.setPassword(user.getPassword());
		request.setAccount(account);
		try {
			InputStream in = HttpRequest.postData(Constant.USER_LOGIN_URL, request.build().toByteArray());
			if (in != null) {
				SignInResponse resp = SignInResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (resp.getResponse().getResult().getNumber() == 1) {
					UserDefaultInfo.setUserToken(context, resp.getToken());
					user.setNickname(resp.getNickName());
					return resp.getResponse().getResult().getNumber();
				} else {
					return resp.getErrorNumber();
				}
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -2;
	}
	
	/**
	 * 
	 * @param user
	 * @return 10 success  -10 local error 
	 */
	public int userRegister(User user,String[] token){
		SignUpRequest.Builder request = SignUpRequest.newBuilder();
		request.setPassWord(user.getPassword());
		request.setPhoneOrEmail(user.getUsername());
		request.setNickName(user.getNickname());
		try {
			InputStream in = HttpRequest.postData(Constant.USER_REGISTER_URL, request.build().toByteArray());
			if (in != null) {
				SignUpResponse resp = SignUpResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (resp.getResponse().getResult().getNumber() == 1) {
					token[0] = resp.getToken();
					return 10;
				}else {
					if(resp.getResponse().getResult()==ResultType.FAIL){
						return -10;
					}
					return resp.getErrorNumber();
				}
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -10;
	}
	
	public int userWeiboLogin(WeiboInfo weiboInfo){
		WeiboSignInRequest.Builder request = WeiboSignInRequest.newBuilder();
		request.setUserId(Long.parseLong(weiboInfo.getUserId()));
		request.setUserName(weiboInfo.getUserName());
		request.setTokenSecret(weiboInfo.getTokenSecret());
		request.setOauthToken(weiboInfo.getToken());
		request.setType(Type.SINA);
		try {
			InputStream in = HttpRequest.postData(Constant.USER_WEIBO_LOGIN_URL, request.build().toByteArray());
			if (in != null) {
				SignInResponse resp = SignInResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (resp.getResponse().getResult().getNumber() == 1) {
					UserDefaultInfo.setUserToken(context, resp.getToken());
					return 10;
				}else {
					return resp.getErrorNumber();
				}
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -10;
	}
	
	public int editUserNickname(String token,String nickname){
		EditNickNameRequest.Builder request = EditNickNameRequest.newBuilder();
		request.setNickName(nickname);
		request.setToken(token);
		try {
			InputStream in = HttpRequest.postData(Constant.USER_EDIT_NICKNAME_URL, request.build().toByteArray());
			if (in != null) {
				EditResponse resp = EditResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (resp.getResponse().getResult().getNumber() == 1) {
					return 10;
				}else {
					return resp.getErrorNumber();
				}
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -10;
	}
	
	public int editUserPassword(String oldPassword,String token,String newPassword){
		EditPassWordRequest.Builder request = EditPassWordRequest.newBuilder();
		request.setOldPassWord(oldPassword);
		request.setPassWord(newPassword);
		request.setToken(token);
		try {
			InputStream in = HttpRequest.postData(Constant.USER_EDIT_PASSWORD_URL, request.build().toByteArray());
			if (in != null) {
				EditResponse resp = EditResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (resp.getResponse().getResult().getNumber() == 1) {
					return 10;
				}else {
					return resp.getErrorNumber();
				}
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -10;
	}
	
	public int editUserAvatar(String token,byte[] data){
		EditIconRequest.Builder request = EditIconRequest.newBuilder();
		request.setIcon(ByteString.copyFrom(data));
		request.setToken(token);
		try {
			InputStream in = HttpRequest.postData(Constant.USER_EDIT_AVATER_URL, request.build().toByteArray());
			if (in != null) {
				EditResponse resp = EditResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (resp.getResponse().getResult().getNumber() == 1) {
					return 10;
				}else {
					return resp.getErrorNumber();
				}
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -10;
	}
	
	public int findPasswordByEmail(String email){
		UserForgotPasswordRequest.Builder request = UserForgotPasswordRequest.newBuilder();
		request.setEmail(email);
		try {
			InputStream in = HttpRequest.postData(Constant.USER_FIND_PASSWORD_BY_EMAIL, request.build().toByteArray());
			if (in != null) {
				UserForgotPasswordResponse resp = UserForgotPasswordResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				if (resp.getResponse().getResult()==ResultType.SUCCESS) {
					return 10;
				} else if (resp.getResponse().getResult() == ResultType.FAIL) {
					return 1;
				} else if (resp.getResponse().getResult() == ResultType.DATAISNULL) {
					return 2;
				}
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -10;
	}
	
	public void userStatistics(Client.Builder client){
		ClientRequest.Builder request = ClientRequest.newBuilder();
		request.setClient(client);
		try {
			InputStream in = HttpRequest.postData(Constant.USER_STATISTICS, request.build().toByteArray());
			if (in != null) {
				ClientResponse resp = ClientResponse.parseFrom(Base64.decodeBase64(ConvertUtils.InputStreamToByte(in)));
				Log.d("Statistics", resp.getResponse().getResult().getNumber()+"");
			}
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
