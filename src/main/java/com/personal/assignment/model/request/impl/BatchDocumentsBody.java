package com.personal.assignment.model.request.impl;

import java.util.List;

public record BatchDocumentsBody(String author, List<String> titles) {
}
