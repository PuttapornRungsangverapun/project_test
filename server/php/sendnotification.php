<?php

function sendnoti($user_id,$user_id_friend,$topic,$message){
include("db_con.php");
$mysql_qry = "select token_body from tokens_notification where user_id = '$user_id'";
$result = mysqli_query($conn ,$mysql_qry);
$row=mysqli_fetch_array($result);
$token_noti=$row['token_body'];
	exec('curl -X POST --header "Authorization: key=AAAA7E_TXOo:APA91bG8g5-jIpjhROAm_MZZBzxQWOlzbiTBPDy43InqvVIsHzTI442Y9KU4mlpnR2u15dQo76w1w2xK2viTAd3enIQh11ryx0ONoP9P4kU1VOkqFMvWguDAyTAWWLcjRg0ysqOfpsar4EqeWTb5_NDAk4nC_9nO6A" --Header "Content-Type: application/json" https://fcm.googleapis.com/fcm/send -d "{\"to\":\"'.$token_noti.'\",\"data\":{\"title\":\"'.$topic.'\",\"body\":\"'.$message.'\",\"tag\":\"'.$user_id_friend.'\"},\"priority\":10}"');
}

?>