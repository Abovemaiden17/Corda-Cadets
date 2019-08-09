//package com.template.flows.nonfungibletokentest
//
//TokenAmount usdAmount = new TokenAmount();
//usdAmount.setAmount(Long.valueOf(100));
//usdAmount.setTokenType(new TokenType("USD",Long.valueOf(2)));
//TokenAmount phpAmount = new TokenAmount();
//phpAmount.setAmount(Long.valueOf(100));
//phpAmount.setTokenType(new TokenType("PHP",Long.valueOf(2)));
//List<TokenAmount> listTokenAmount = new ArrayList<TokenAmount>();
//listTokenAmount.add(usdAmount);
//listTokenAmount.add(phpAmount);
//System.out.println("<<BEFORE>>");
//for(TokenAmount tokenAmount : listTokenAmount) {
//    System.out.println(tokenAmount.getAmount() +" -- " + tokenAmount.getTokenType().getTokenIdentifier());
//}
//List<TokenAmount> filteredListTokenAmount =  listTokenAmount.stream().filter(x-> x.getTokenType().getTokenIdentifier() == "PHP").collect(Collectors.toList());
//listTokenAmount.remove(filteredListTokenAmount.get(0));
//filteredListTokenAmount.get(0).setAmount(filteredListTokenAmount.get(0).getAmount() + 100);
//TokenAmount added = filteredListTokenAmount.get(0);
//listTokenAmount.add(added);
//System.out.println("<<AFTER>>");
//for(TokenAmount tokenAmount : listTokenAmount) {
//    System.out.println(tokenAmount.getAmount() +" -- " + tokenAmount.getTokenType().getTokenIdentifier());
//}