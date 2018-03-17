pragma solidity ^0.4.19;
contract repnet {
    
    struct Rating {
        address sender;
        address receipient;
        uint8 score;
    }
        
    event RateEvent (
        address sender,
        address receipient,
        uint8 score
    );
    
    Rating[] RatingList;
    // public mapping(address => mapping(address => uint8)) RepMap;
    // public address[] Senders;
    // public mapping(address => bool) IsSender;
    // public mapping(address => address[]) SenderToReceipients;

    function rate(address _receipient, uint8 _score) public {
        require(_score > 0 && _score < 6 && msg.sender != _receipient);
        RatingList.push(Rating(msg.sender, _receipient, _score));
        RateEvent(msg.sender, _receipient, _score);
    }
    
//    function getRatings() public view returns (Rating[]) {
 //       return RatingList;
   // }
    
    // function Rate(address _receipient, uint8 _score) public {
    //     require(score > 0 && score < 6);
    //     if (!IsSender[msg.sender]) {
    //         IsSender[msg.sender] = true;
    //         Senders.push(msg.sender);
    //     }
    //     if (RepMap[msg.sender][receipient] == 0) {
    //         SenderToReceipients[msg.sender].push(_receipient)
    //     }
    //     RepMap[msg.sender][_receipient] = _score;
    //     emit RateEvent(msg.sender, _receipient, _score);
    // }

    // function getRating(address _sender, address _receipient) public pure returns (uint8) {
    //     return RepMap[_sender][_receipient];
    // }
    
    // function getAllRatings() public view returns (Rating[]) {
    //     Rating[1000] memory ret;
    //     for (uint i = 0; i < Senders.length; i++) {
    //         for (uint j = 0; j < SenderToReceipients.length; j++) {
    //              ret.push(Rating(Senders[i], SenderToReceipients[Senders[i]][j], _score))
    //         }
    //     }
    //     return ret
    // }
}
